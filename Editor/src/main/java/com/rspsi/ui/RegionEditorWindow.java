package com.rspsi.ui;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.displee.util.GZIPUtils;

import com.displee.cache.index.Index;
import com.jagex.Client;
import com.jagex.cache.loader.map.MapIndexLoader;
import com.jagex.cache.loader.map.MapType;
import com.rspsi.cache.CacheFileType;
import com.rspsi.resources.ResourceLoader;
import com.rspsi.util.FXDialogs;
import com.rspsi.util.FXUtils;
import com.rspsi.util.FilterMode;
import com.rspsi.util.RetentionFileChooser;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * Builds a brand new map index: an empty grid of regions where every cell can be
 * filled with an existing region from the cache, or left empty. The result is
 * written as a .pack file, which can be opened and edited with "Open from > .pack".
 */
@Slf4j
public class RegionEditorWindow extends Application {

	private static final int MAX_SIZE = 16;
	private static final int CELL_SIZE = 72;

	/**
	 * A region from the cache, or {@link #EMPTY}.
	 */
	public static final class RegionEntry {
		public static final RegionEntry EMPTY = new RegionEntry(-1, -1, -1);

		public final int hash;
		public final int landscapeId;
		public final int objectId;

		/** Map data taken from an opened .pack, null for regions read from the cache */
		public final byte[] packTiles;
		public final byte[] packObjects;
		private final String packName;

		RegionEntry(int hash, int landscapeId, int objectId) {
			this.hash = hash;
			this.landscapeId = landscapeId;
			this.objectId = objectId;
			this.packTiles = null;
			this.packObjects = null;
			this.packName = null;
		}

		RegionEntry(String packName, int landscapeId, int objectId, byte[] tiles, byte[] objects) {
			this.hash = -1;
			this.landscapeId = landscapeId;
			this.objectId = objectId;
			this.packTiles = tiles;
			this.packObjects = objects;
			this.packName = packName;
		}

		public boolean isFromPack() {
			return packTiles != null;
		}

		public boolean isEmpty() {
			return hash == -1 && !isFromPack();
		}

		public int regionX() {
			return hash >> 8;
		}

		public int regionY() {
			return hash & 0xff;
		}

		public String shortName() {
			if (isFromPack())
				return packName + "\n(pack)";
			return isEmpty() ? "Empty" : regionX() + "_" + regionY();
		}

		@Override
		public String toString() {
			if (isEmpty())
				return "<Empty region>";
			return String.format("%d_%d  (id %d, tile %d,%d)", regionX(), regionY(), hash, regionX() * 64, regionY() * 64);
		}
	}

	private Stage stage;
	private Consumer<byte[]> openInEditor;

	private final ObservableList<RegionEntry> regions = FXCollections.observableArrayList();
	private final FilteredList<RegionEntry> filteredRegions = new FilteredList<>(regions, r -> true);

	/** grid[x][y], y = 0 is the southern row */
	private RegionEntry[][] grid = new RegionEntry[0][0];

	private final GridPane gridPane = new GridPane();
	private final ToggleGroup cellGroup = new ToggleGroup();
	private final ListView<RegionEntry> regionList = new ListView<>(filteredRegions);
	private final Spinner<Integer> widthSpinner = new Spinner<>();
	private final Spinner<Integer> lengthSpinner = new Spinner<>();
	private final Label statusLabel = new Label();

	private int selectedX = 0, selectedY = 0;

	public RegionEditorWindow(Consumer<byte[]> openInEditor) {
		this.openInEditor = openInEditor;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.stage = primaryStage;

		widthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, MAX_SIZE, 2));
		lengthSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, MAX_SIZE, 2));
		widthSpinner.setEditable(true);
		lengthSpinner.setEditable(true);
		widthSpinner.setPrefWidth(70);
		lengthSpinner.setPrefWidth(70);
		FXUtils.addSpinnerFocusListeners(widthSpinner, lengthSpinner);
		widthSpinner.valueProperty().addListener((obs, o, n) -> resizeGrid());
		lengthSpinner.valueProperty().addListener((obs, o, n) -> resizeGrid());

		Button newBtn = new Button("New (all empty)");
		newBtn.setOnAction(evt -> {
			grid = new RegionEntry[0][0];
			resizeGrid();
		});

		Button openPackBtn = new Button("Open .pack...");
		openPackBtn.setOnAction(evt -> openPack());

		HBox sizeBox = new HBox(6, new Label("Width:"), widthSpinner, new Label("Length:"), lengthSpinner, newBtn, openPackBtn);
		sizeBox.setAlignment(Pos.CENTER_LEFT);

		gridPane.setHgap(2);
		gridPane.setVgap(2);
		gridPane.setPadding(new Insets(4));
		ScrollPane gridScroll = new ScrollPane(gridPane);
		gridScroll.setPrefViewportWidth(CELL_SIZE * 6);
		gridScroll.setPrefViewportHeight(CELL_SIZE * 6);

		Label gridHint = new Label("Select a cell, then double-click a region (or press Assign). North is up.");
		VBox gridBox = new VBox(6, sizeBox, gridHint, gridScroll);
		VBox.setVgrow(gridScroll, Priority.ALWAYS);

		TextField searchField = new TextField();
		searchField.setPromptText("Search, e.g. 50_50 or 12850");
		searchField.textProperty().addListener((obs, o, text) -> {
			String query = text == null ? "" : text.trim().replace(',', '_').replace(' ', '_');
			filteredRegions.setPredicate(r -> query.isEmpty() || r.isEmpty() || r.toString().contains(query));
		});

		regionList.setPrefWidth(280);
		regionList.setOnMouseClicked(evt -> {
			if (evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() == 2)
				assignSelected();
		});

		Button assignBtn = new Button("Assign");
		assignBtn.setOnAction(evt -> assignSelected());
		Button clearBtn = new Button("Make empty");
		clearBtn.setOnAction(evt -> assign(RegionEntry.EMPTY));
		Button reloadBtn = new Button("Reload list");
		reloadBtn.setOnAction(evt -> loadRegionList());
		HBox listButtons = new HBox(6, assignBtn, clearBtn, reloadBtn);

		VBox listBox = new VBox(6, new Label("Regions in cache"), searchField, regionList, listButtons);
		VBox.setVgrow(regionList, Priority.ALWAYS);

		Button saveBtn = new Button("Save as .pack...");
		saveBtn.setOnAction(evt -> savePack());
		Button openBtn = new Button("Open in editor");
		openBtn.setOnAction(evt -> openInEditor());
		HBox bottom = new HBox(8, statusLabel, new HBox(), saveBtn, openBtn);
		HBox.setHgrow(bottom.getChildren().get(1), Priority.ALWAYS);
		bottom.setAlignment(Pos.CENTER_LEFT);

		BorderPane root = new BorderPane();
		root.setPadding(new Insets(8));
		root.setCenter(gridBox);
		root.setRight(listBox);
		root.setBottom(bottom);
		BorderPane.setMargin(listBox, new Insets(0, 0, 0, 8));
		BorderPane.setMargin(bottom, new Insets(8, 0, 0, 0));

		primaryStage.setTitle("Region Editor - build a new map index");
		primaryStage.setScene(new Scene(root));
		primaryStage.getIcons().add(ResourceLoader.getSingleton().getLogo64());
		FXUtils.centerStage(primaryStage);

		resizeGrid();
	}

	public void show() {
		if (regions.isEmpty())
			loadRegionList();
		stage.show();
		stage.toFront();
	}

	/**
	 * Lists every region that has a landscape file in the cache's map index.
	 */
	private void loadRegionList() {
		List<RegionEntry> found = new ArrayList<>();
		try {
			Index mapIndex = Client.getSingleton().getCache().getFile(CacheFileType.MAP);
			Set<Integer> groups = new HashSet<>();
			for (int id : mapIndex.archiveIds())
				groups.add(id);

			for (int x = 0; x < 256; x++) {
				for (int y = 0; y < 256; y++) {
					int landscapeId = MapIndexLoader.getLandscapeId(x, y);
					if (landscapeId == -1 || !groups.contains(landscapeId))
						continue;
					int objectId = MapIndexLoader.getObjectId(x, y);
					found.add(new RegionEntry((x << 8) + y, landscapeId, objectId));
				}
			}
		} catch (Exception ex) {
			log.error("Failed to list regions", ex);
			FXDialogs.showError(stage, "Error while listing regions", "Could not read the map index from the cache.\nIs a cache loaded?");
		}
		found.add(0, RegionEntry.EMPTY);
		regions.setAll(found);
		statusLabel.setText((found.size() - 1) + " regions found in cache");
	}

	private void resizeGrid() {
		int width = widthSpinner.getValue();
		int length = lengthSpinner.getValue();
		RegionEntry[][] resized = new RegionEntry[width][length];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < length; y++) {
				boolean kept = x < grid.length && y < grid[x].length;
				resized[x][y] = kept ? grid[x][y] : RegionEntry.EMPTY;
			}
		}
		grid = resized;
		selectedX = Math.min(selectedX, width - 1);
		selectedY = Math.min(selectedY, length - 1);
		rebuildGrid();
	}

	private void rebuildGrid() {
		gridPane.getChildren().clear();
		cellGroup.getToggles().clear();
		int length = grid[0].length;
		for (int x = 0; x < grid.length; x++) {
			for (int y = 0; y < length; y++) {
				final int cellX = x, cellY = y;
				RegionEntry entry = grid[x][y];
				ToggleButton cell = new ToggleButton("[" + x + "," + y + "]\n" + entry.shortName());
				cell.setMinSize(CELL_SIZE, CELL_SIZE);
				cell.setMaxSize(CELL_SIZE, CELL_SIZE);
				cell.setStyle(entry.isEmpty() ? "-fx-opacity: 0.6;"
						: entry.isFromPack() ? "-fx-font-style: italic;" : "-fx-font-weight: bold;");
				cell.setToggleGroup(cellGroup);
				cell.setSelected(x == selectedX && y == selectedY);
				cell.setOnAction(evt -> {
					selectedX = cellX;
					selectedY = cellY;
					cell.setSelected(true);
				});
				// row 0 at the top is the most northern row
				gridPane.add(cell, x, length - 1 - y);
			}
		}
	}

	private void assignSelected() {
		RegionEntry entry = regionList.getSelectionModel().getSelectedItem();
		if (entry != null)
			assign(entry);
	}

	private void assign(RegionEntry entry) {
		grid[selectedX][selectedY] = entry;
		rebuildGrid();
	}

	private byte[] buildPack() {
		List<byte[]> encoded = new ArrayList<>();
		int total = 4;
		int count = 0;
		for (int x = 0; x < grid.length; x++) {
			for (int y = 0; y < grid[x].length; y++) {
				RegionEntry entry = grid[x][y];
				byte[] tiles = null;
				byte[] objects = null;
				if (entry.isFromPack()) {
					tiles = entry.packTiles;
					objects = entry.packObjects;
				} else if (!entry.isEmpty()) {
					tiles = readMap(entry.landscapeId, 0, entry.hash);
					objects = readMap(entry.objectId, 1, entry.hash);
					if (tiles == null)
						throw new IllegalStateException("Could not read landscape of region " + entry.shortName());
				}
				if (tiles == null)
					tiles = emptyTiles();
				if (objects == null)
					objects = emptyObjects();

				// same layout as MultiMapEncoder
				ByteBuffer buffer = ByteBuffer.allocate(24 + tiles.length + objects.length);
				buffer.putInt(entry.objectId);
				buffer.putInt(entry.landscapeId);
				buffer.putInt(x);
				buffer.putInt(y);
				buffer.putInt(objects.length);
				buffer.put(objects);
				buffer.putInt(tiles.length);
				buffer.put(tiles);
				encoded.add(buffer.array());
				total += buffer.capacity();
				count++;
			}
		}

		ByteBuffer pack = ByteBuffer.allocate(total);
		pack.putInt(count);
		encoded.forEach(pack::put);
		return pack.array();
	}

	private static byte[] readMap(int group, int file, int hash) {
		if (group == -1)
			return null;
		try {
			byte[] data = Client.getSingleton().getCache().readMap(group, file, hash);
			if (data == null)
				return null;
			byte[] unzipped = GZIPUtils.unzip(data);
			return unzipped == null ? data : unzipped;
		} catch (Exception ex) {
			log.warn("Failed to read map group {} file {}", group, file, ex);
			return null;
		}
	}

	/**
	 * A flat region: plane 0 has underlay 1 at height 0, upper planes use the default height.
	 */
	private static byte[] emptyTiles() {
		ByteBuffer buffer = ByteBuffer.allocate(64 * 64 * 7 + 3 * 64 * 64 * 2);
		for (int z = 0; z < 4; z++) {
			for (int i = 0; i < 64 * 64; i++) {
				if (z == 0) {
					buffer.putShort((short) (1 + 81)); // underlay 1
					buffer.putShort((short) 1); // manual height
					buffer.put((byte) 0);
				} else {
					buffer.putShort((short) 0);
				}
			}
		}
		return Arrays.copyOf(buffer.array(), buffer.position());
	}

	private static byte[] emptyObjects() {
		return new byte[2];
	}

	private void savePack() {
		File file = RetentionFileChooser.showSaveDialog("Enter a name for packed maps file...", stage, "", FilterMode.PACK);
		if (file == null)
			return;
		try {
			Files.write(file.toPath(), buildPack());
			statusLabel.setText("Saved " + file.getName());
		} catch (IOException | RuntimeException ex) {
			log.error("Failed to save pack", ex);
			FXDialogs.showError(stage, "Error while saving map!", "There was an error while writing the packed maps file:\n" + ex.getMessage());
		}
	}

	/**
	 * Rebuilds the grid from an existing .pack. Cells keep the pack's own map data,
	 * so edits made in the main editor are preserved.
	 */
	private void openPack() {
		File file = RetentionFileChooser.showOpenDialog(stage, FilterMode.PACK);
		if (file == null || !file.exists())
			return;
		if (regions.isEmpty())
			loadRegionList();
		try {
			ByteBuffer buffer = ByteBuffer.wrap(Files.readAllBytes(file.toPath()));
			int count = buffer.getInt();
			List<int[]> positions = new ArrayList<>();
			List<RegionEntry> entries = new ArrayList<>();
			int width = 1, length = 1;
			for (int i = 0; i < count; i++) {
				int objectId = buffer.getInt();
				int landscapeId = buffer.getInt();
				int x = buffer.getInt();
				int y = buffer.getInt();
				byte[] objects = new byte[buffer.getInt()];
				buffer.get(objects);
				byte[] tiles = new byte[buffer.getInt()];
				buffer.get(tiles);
				if (x < 0 || y < 0)
					throw new IOException("Invalid region position " + x + "," + y);

				positions.add(new int[] { x, y });
				entries.add(new RegionEntry(packName(landscapeId), landscapeId, objectId, tiles, objects));
				width = Math.max(width, x + 1);
				length = Math.max(length, y + 1);
			}

			RegionEntry[][] loaded = new RegionEntry[width][length];
			for (RegionEntry[] column : loaded)
				Arrays.fill(column, RegionEntry.EMPTY);
			for (int i = 0; i < entries.size(); i++)
				loaded[positions.get(i)[0]][positions.get(i)[1]] = entries.get(i);

			// resize first: each spinner change resizes the grid using the other spinner's
			// (old) value, which would cut off part of the loaded grid
			ensureSpinnerMax(widthSpinner, width);
			ensureSpinnerMax(lengthSpinner, length);
			widthSpinner.getValueFactory().setValue(width);
			lengthSpinner.getValueFactory().setValue(length);
			grid = loaded;
			selectedX = 0;
			selectedY = 0;
			rebuildGrid();
			statusLabel.setText("Opened " + file.getName() + " (" + count + " regions)");
		} catch (IOException | RuntimeException ex) {
			log.error("Failed to open pack", ex);
			FXDialogs.showError(stage, "Error while loading map!", "There was an error while loading or parsing the selected file.");
		}
	}

	private String packName(int landscapeId) {
		if (landscapeId == -1)
			return "Empty";
		return regions.stream()
				.filter(r -> !r.isEmpty() && r.landscapeId == landscapeId)
				.map(r -> r.regionX() + "_" + r.regionY())
				.findFirst()
				.orElse("#" + landscapeId);
	}

	private static void ensureSpinnerMax(Spinner<Integer> spinner, int value) {
		SpinnerValueFactory.IntegerSpinnerValueFactory factory = (SpinnerValueFactory.IntegerSpinnerValueFactory) spinner.getValueFactory();
		if (factory.getMax() < value)
			factory.setMax(value);
	}

	private void openInEditor() {
		try {
			openInEditor.accept(buildPack());
		} catch (RuntimeException ex) {
			log.error("Failed to build pack", ex);
			FXDialogs.showError(stage, "Error while building map!", ex.getMessage());
		}
	}

}
