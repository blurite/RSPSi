package com.rspsi.controllers;

import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import com.jfoenix.controls.JFXButton;
import com.rspsi.util.Settings;
import javafx.scene.control.*;
import com.rspsi.tools.MountainGenerator;
import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.canvas.Canvas;
import org.major.map.RenderFlags;

import com.google.common.primitives.Doubles;
import com.jagex.Client;
import com.jagex.map.SceneGraph;
import com.jagex.util.BitFlag;
import com.jfoenix.controls.JFXCheckBox;
import com.rspsi.ui.MainWindow;
import com.rspsi.ui.Testing;
import com.rspsi.controls.SwatchControl;
import com.rspsi.controls.WindowControls;
import com.rspsi.core.misc.BrushType;
import com.rspsi.core.misc.ToolType;
import com.rspsi.options.Options;
import com.rspsi.swatches.SwatchType;
import com.rspsi.util.AlwaysSelectToggleGroup;
import com.rspsi.util.ChangeListenerUtil;
import com.rspsi.util.FXDialogs;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.HPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.converter.IntegerStringConverter;
import lombok.Getter;

@Getter
public class MainController {

	@FXML
	private MenuItem changeViewDist;

	@FXML
	private MenuItem openAsPackBtn;

	@FXML
	private MenuItem saveToCacheBtn;

	@FXML
	private MenuItem saveAsPackFile;

	@FXML
	private MenuItem showMapIndexEditor;

	@FXML
	private MenuItem showRegionEditor;

	@FXML
	private HBox grabBar;

	@FXML
	private ImageView iconView;

	@FXML
	private HBox controlBox;

	@FXML
	private Label titleLabel;

	@FXML
	private Label statusLabel;

	@FXML
	private MenuBar toolbar;

	@FXML
	private MenuItem newMapButton;

	@FXML
	private Menu openRecentMenu;

	@FXML
	private MenuItem openCoordinateButton;

	@FXML
	private MenuItem openHashButton;

	@FXML
	private MenuItem openFileButton;

	@FXML
	private MenuItem saveMenuItem;

	@FXML
	private MenuItem saveAsMenuItem;

	@FXML
	private MenuItem preferencesMenuItem;

	@FXML
	private MenuItem quitMenuItem;

	@FXML
	private MenuItem copySelectedTilesBtn;

	@FXML
	private MenuItem deleteSelectedTilesBtn;

	@FXML
	private MenuItem pasteTilesBtn;

	@FXML
	private MenuItem addObjectToSwatchBtn;

	@FXML
	private CheckMenuItem hiddenTilesCheckItem;

	@FXML
	private CheckMenuItem allHeightsCheckItem;

	@FXML
	private CheckMenuItem disableBlendingCheckItem;

	@FXML
	private CheckMenuItem showObjectsCheckItem;

	@FXML
	private CheckMenuItem rememberSize;

	@FXML
	private CheckMenuItem rememberLocation;

	@FXML
	private CheckMenuItem saveByGroupName;

	@FXML
	private CheckMenuItem showOverlaysCheckItem;

	@FXML
	private MenuItem forceMapUpdateBtn;

	@FXML
	private MenuItem importTilesBtn;

	@FXML
	private MenuItem exportTilesBtn;

	@FXML
	private MenuItem showObjectViewBtn;

	@FXML
	private CheckMenuItem showFPSCheckItem;
	@FXML
	private CheckMenuItem showTileDataCheckItem;

	@FXML
	private MenuItem openTutorialBtn;

	@FXML
	private MenuItem contactMeBtn;

	@FXML
	private HBox dockContainer;

	@FXML
	private AnchorPane leftBar;

	@FXML
	private ToggleButton selectTileBtn;

	@FXML
	private ToggleGroup toolGroup;

	@FXML
	private ToggleButton selectObjectBtn;

	@FXML
	private ToggleButton paintTileBtn;

	@FXML
	private ToggleButton heightModifyBtn;

	@FXML
	private ToggleButton deleteObjectBtn;

	@FXML
	private ToggleButton paintFillBtn;

	@FXML
	private ToggleButton heightFillBtn;

	@FXML
	private ToggleButton moveObjectBtn;

	@FXML
	private JFXButton returnToLauncher;


	@FXML
	private AnchorPane gamePane;

	@FXML
	private AnchorPane mapPane;

	@FXML
	private Slider brushSizeSlider;

	@FXML
	private ComboBox<BrushType> brushTypeSelection;

	@FXML
	private Slider heightLevelSlider;

	@FXML
	private ComboBox<String> objectSelectionType;

	@FXML
	private Spinner<Integer> currentHeightSpinner;

	@FXML
	private AnchorPane rightBar;

	@FXML
	private Tooltip brushSizeTooltip;

	@FXML
	private Tooltip heightLevelTooltip;

	@FXML
	private Button decreaseBrushSizeBtn;

	@FXML
	private Button increaseBrushSizeBtn;

	@FXML
	private TabPane toolsTabPane;

	private Tab mountainTab;
	private Tab coastTab;
	private Tab bridgeTab;

	@FXML
	private MenuItem undoMenuItem;

	@FXML
	private MenuItem redoMenuItem;

	@FXML
	private CheckMenuItem showBlockedFlag;

	@FXML
	private CheckMenuItem showBridgeFlag;

	@FXML
	private CheckMenuItem showLowestFlag;

	@FXML
	private CheckMenuItem showDisableFlag;

	@FXML
	private JFXCheckBox unwalkableCheck;

	@FXML
	private JFXCheckBox bridgeCheck;

	@FXML
	private JFXCheckBox forceLowestCheck;
	
	@FXML
	private JFXCheckBox disableRenderCheck;

	@FXML
	private JFXCheckBox drawOnLowerZCheck;
	

	@FXML
	private JFXCheckBox absoluteHeightCheck;

	@FXML
	private ToggleButton setFlagBtn;
	
    @FXML
    private ToggleButton paintOverlayBtn;

    @FXML
    private ToggleButton paintPathBtn;

    @FXML
    private ToggleButton mountainBtn;

    @FXML
    private ToggleButton coastBtn;

    @FXML
    private ToggleButton bridgeBtn;

    @FXML
    private VBox rightPanel;

    @FXML
    private ToggleButton paintUnderlayBtn;

	@FXML
	private MenuItem copyTileFlags;


	@FXML
	private CheckMenuItem showLowerZFlag;


	@FXML
	private MenuItem copyTileHeights;

	@FXML
	private TextField tileHeightTextBox;

	@FXML
	private Button tileHeightTextButton;

	@FXML
	private MenuItem setFlagsToTiles;

	@FXML
	private MenuItem setHeightsToTiles;

	@FXML
	private MenuItem showFullMap;

	@FXML
	private MenuItem reloadSwatchesBtn;
	@FXML
	private MenuItem reloadModelsBtn;

	@FXML
	private MenuItem fixHeightsBtn;

	@FXML
	private MenuItem getOverlayFromTile;

	@FXML
	private MenuItem getUnderlayFromTile;

	@FXML
	private CheckMenuItem displayOverlayIds;

	@FXML
	private CheckMenuItem displayUnderlayIds;

	@FXML
	private CheckMenuItem displayTileHeights;

	@FXML
	private CheckMenuItem showMapIconObjs;

	@FXML
	private CheckMenuItem showAnimsMenuItem;

	@FXML
	private TabPane mainTabPane;

	@FXML
	private MenuItem setOverlays;

	@FXML
	private MenuItem setUnderlays;

	@FXML
	private CheckMenuItem simulateBridges;

	@FXML
	private MenuItem showRemapperBtn;

	@FXML
	private MenuItem convertLandscapeBtn;

	@FXML
	private MenuItem setRelativeHeight;
	
	@FXML
	private VBox root;
	

    @FXML
    private Menu fileMenu, editMenu, viewMenu, tilesMenu, toolsMenu, helpMenu, debugMenu;

	public void initializeToolButtons() {
		this.toolGroup.selectedToggleProperty().addListener((observable, oldVal, newVal) -> {
			if (newVal == null) {
				if (toolGroup.getProperties().get("deselect") == null) {
					oldVal.setSelected(true);
				} else {
					toolGroup.getProperties().remove("deselect");
				}
			}
		});
		ChangeListenerUtil.addListener(() -> SceneGraph.mouseWasDown = true, Options.currentTool);
		ChangeListenerUtil.addListener(true, () -> {
			Options.currentTool.set(ToolType.SELECT_TILE);
		}, selectTileBtn.selectedProperty());

		ChangeListenerUtil.addListener(true, () -> {
			Options.currentTool.set(ToolType.MODIFY_HEIGHT);
		}, heightModifyBtn.selectedProperty());

		ChangeListenerUtil.addListener(true, () -> {
			Options.currentTool.set(ToolType.SELECT_OBJECT);
		}, selectObjectBtn.selectedProperty());
		ChangeListenerUtil.addListener(true, () -> {
			Options.currentTool.set(ToolType.DELETE_OBJECT);
		}, deleteObjectBtn.selectedProperty());
		
		ChangeListenerUtil.addListener(true, () -> {
			Options.currentTool.set(ToolType.PAINT_OVERLAY);
		}, paintOverlayBtn.selectedProperty());
		ChangeListenerUtil.addListener(true, () -> {
			Options.currentTool.set(ToolType.PAINT_PATH);
		}, paintPathBtn.selectedProperty());
		ChangeListenerUtil.addListener(true, () -> {
			Options.currentTool.set(ToolType.MOUNTAIN);
		}, mountainBtn.selectedProperty());
		ChangeListenerUtil.addListener(true, () -> {
			Options.currentTool.set(ToolType.COAST);
		}, coastBtn.selectedProperty());
		ChangeListenerUtil.addListener(true, () -> {
			Options.currentTool.set(ToolType.BRIDGE);
		}, bridgeBtn.selectedProperty());
		ChangeListenerUtil.addListener(true, () -> {
			Options.currentTool.set(ToolType.PAINT_UNDERLAY);
		}, paintUnderlayBtn.selectedProperty());
		
		ChangeListenerUtil.addListener(true, () -> {
			Options.currentTool.set(ToolType.SET_FLAGS);
		}, setFlagBtn.selectedProperty());
		

	}

	public void loadTabs(MainWindow application) {

		for (int i = 0; i < 3; i++) {
			SwatchType swatchType = SwatchType.getById(i);
			SwatchControl swatch = new SwatchControl(swatchType);
			switch (swatchType) {
			case OBJECT:
				application.setObjectSwatch(swatch);
				break;
			case OVERLAY:

				FlowPane flowPane = new FlowPane();
				flowPane.setPadding(new Insets(4, 4, 4, 4));
				flowPane.setOrientation(Orientation.HORIZONTAL);
				flowPane.setRowValignment(VPos.CENTER);
				flowPane.setColumnHalignment(HPos.CENTER);
				flowPane.setHgap(6);
				flowPane.setVgap(6);
				ToggleGroup tg = new ToggleGroup();

				AlwaysSelectToggleGroup.setup(tg);
				ToggleButton[] shapeButtons = new ToggleButton[13];
				for (int type = 0; type < 13; type++) {
					Pane g = Testing.generateImage(type);
					ToggleButton btn = new ToggleButton();
					shapeButtons[type] = btn;
					tg.getProperties().put(type, btn);
					btn.setAlignment(Pos.CENTER);
					btn.setMaxSize(36, 36);
					btn.setPrefSize(36, 36);
					btn.setMinSize(36, 36);
					btn.setToggleGroup(tg);
					btn.setGraphic(g);
					btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
					final int shapeType = type;
					btn.setOnAction(evt -> {

						Options.overlayPaintShapeId.set(shapeType);
					});
					flowPane.getChildren().add(btn);
					if (type == 1) {
						btn.setSelected(true);
					}

				}
				flowPane.setAlignment(Pos.CENTER);
				swatch.getController().getVboxContainer().setSpacing(10);
				swatch.setOverlayShapeGroup(tg);
				swatch.getController().getVboxContainer().getChildren().add(0, flowPane);
				application.setOverlaySwatch(swatch);

				//TODO Find a less aids way of doing this

				break;
			case UNDERLAY:
				application.setUnderlaySwatch(swatch);
				break;

			}

			Tab tab = new Tab(swatchType.toString());
			tab.setContent(swatch);
			toolsTabPane.getTabs().add(i, tab);
		}

		toolsTabPane.getTabs().add(createPathTab());
		mountainTab = createMountainTab();
		toolsTabPane.getTabs().add(mountainTab);
		coastTab = createCoastTab();
		toolsTabPane.getTabs().add(coastTab);
		bridgeTab = createBridgeTab();
		toolsTabPane.getTabs().add(bridgeTab);
		toolsTabPane.getSelectionModel().select(SwatchType.OBJECT.getId());

	}

	/** Settings of the overlay path tool. */
	private Tab createPathTab() {
		VBox box = new VBox(14);
		box.setPrefWidth(0); // follow the width of the panel instead of the length of the texts
		box.setPadding(new Insets(10));

		Label intro = hint("Draw a path with the Path tool: click points in the scene, press Enter to apply it "
				+ "or Esc to cancel. The overlay is taken from the Overlay tab.");

		// Shape
		CheckBox smoothCurve = new CheckBox("Smooth curve");
		smoothCurve.selectedProperty().bindBidirectional(Options.pathSmoothCurve);
		VBox shape = section("Shape",
				pathSlider("Width", " tiles", 1, 20, 0.5, Options.pathWidth),
				hint("How wide the path is. 1 is a single tile."),
				smoothCurve,
				hint("On: the path flows as a smooth curve through your points. Off: straight lines between points."),
				pathSlider("Edge smoothing", "", 0, 4, 0.5, Options.pathEdgeSmoothing),
				hint("Makes the shapes of neighbouring tiles line up on their edges. Higher gives a cleaner outline, "
						+ "0 fits every tile on its own."));

		// Terrain
		CheckBox smoothHeight = new CheckBox("Smooth terrain height");
		smoothHeight.selectedProperty().bindBidirectional(Options.pathSmoothHeight);
		VBox heightStrength = pathSlider("Height smoothing", " passes", 1, 250, 1, Options.pathHeightSmoothing);
		heightStrength.disableProperty().bind(Options.pathSmoothHeight.not());
		VBox heightBlend = pathSlider("Blend distance", " tiles", 0, 30, 1, Options.pathHeightBlend);
		heightBlend.disableProperty().bind(Options.pathSmoothHeight.not());
		VBox terrain = section("Terrain",
				smoothHeight,
				hint("Evens out the height under the path so it has no steep steps. Applied together with the path "
						+ "and undone separately (press Ctrl+Z twice)."),
				heightStrength,
				hint("Number of smoothing passes (1-250). Higher values flatten the terrain more along the path; start low and increase if it is still too steep."),
				heightBlend,
				hint("How many tiles around the path are blended into the surrounding terrain. Use a larger distance on steep hillsides so the slope next to the path becomes gradual instead of a sudden wall."));

		box.getChildren().addAll(intro, shape, terrain);

		ScrollPane scroll = new ScrollPane(box);
		scroll.setFitToWidth(true);
		scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scroll.setPrefWidth(0);
		Tab tab = new Tab("Path");
		tab.setContent(scroll);
		return tab;
	}

	/** Settings of the coast tool. */
	private Tab createCoastTab() {
		Label intro = hint("Pick an overlay (the sea), then use the Coast tool: click points along the waterline, "
				+ "press Enter to apply, Esc to cancel. The preview shows the sea side.");
		RadioButton beach = new RadioButton("Beach (slopes down to the sea)");
		RadioButton cliff = new RadioButton("Cliff (steep rock face)");
		ToggleGroup mode = new ToggleGroup();
		beach.setToggleGroup(mode);
		cliff.setToggleGroup(mode);
		cliff.setSelected(Options.coastCliff.get());
		beach.setSelected(!Options.coastCliff.get());
		cliff.selectedProperty().bindBidirectional(Options.coastCliff);

		CheckBox seaLeft = new CheckBox("Sea on the left of the drawing direction");
		seaLeft.selectedProperty().bindBidirectional(Options.coastSeaLeft);

		VBox beachWidth = pathSlider("Beach width", " tiles", 1, 80, 1, Options.coastBeachWidth);
		beachWidth.disableProperty().bind(Options.coastCliff);
		VBox cliffHeight = pathSlider("Cliff height", " units", 50, 1500, 50, Options.coastCliffHeight);
		VBox cliffWidth = pathSlider("Cliff width", " tiles", 1, 8, 1, Options.coastCliffWidth);
		VBox plateau = pathSlider("Plateau depth", " tiles", 2, 40, 1, Options.coastPlateauDepth);
		for (VBox slider : new VBox[] { cliffHeight, cliffWidth, plateau })
			slider.disableProperty().bind(Options.coastCliff.not());

		Button newSeedButton = new Button("New shoreline shape");
		newSeedButton.setOnAction(evt -> Options.coastSeed.set((int) (Math.random() * 1_000_000)));

		VBox box = new VBox(10, intro, beach, cliff, seaLeft,
				pathSlider("Irregularity", " tiles", 0, 8, 0.5, Options.coastIrregularity),
				hint("How much the shoreline meanders: bays and headlands around the line you drew. 0 follows your line exactly."),
				pathSlider("Edge smoothing", "", 0, 4, 0.5, Options.coastEdgeSmoothing),
				newSeedButton,
				pathSlider("Sea width", " tiles", 1, 40, 1, Options.coastSeaWidth),
				hint("How far the sea overlay and the flat sea bed reach from the waterline."),
				beachWidth,
				hint("Beach: land heights slope gently down to 0 over this distance. Use a larger width on high terrain, otherwise the slope stays steep."),
				cliffHeight, cliffWidth, plateau,
				hint("Cliff: the land rises steeply from the waterline to the cliff height and keeps that extra "
						+ "height for the plateau depth, then fades back into the terrain."));
		box.setPadding(new Insets(10));

		ScrollPane scroll = new ScrollPane(box);
		scroll.setFitToWidth(true);
		scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scroll.setPrefWidth(0);
		Tab tab = new Tab("Coast");
		tab.setContent(scroll);
		return tab;
	}

	/** Settings of the bridge tool. */
	private Tab createBridgeTab() {
		Label intro = hint("Pick the height level the bridge crosses and an overlay for the deck, then use the Bridge tool: "
				+ "click points along the bridge, press Enter to build it, Esc to cancel. The bridge is built on the "
				+ "level above (so at most level 2). The preview shows the deck and the ramp around it. "
				+ "Ctrl+Z undoes the whole bridge.");

		CheckBox autoHeight = new CheckBox("Automatic deck height");
		autoHeight.selectedProperty().bindBidirectional(Options.bridgeAutoHeight);
		VBox deckHeight = pathSlider("Deck height", " units", 0, 1500, 10, Options.bridgeDeckHeight);
		deckHeight.disableProperty().bind(Options.bridgeAutoHeight);
		CheckBox flag = new CheckBox("Set bridge flag on the deck");
		flag.selectedProperty().bindBidirectional(Options.bridgeFlag);

		VBox box = new VBox(10, intro,
				pathSlider("Deck width", " tiles", 1, 12, 1, Options.bridgeWidth),
				hint("How wide the bridge is. 1 is a single tile."),
				autoHeight, deckHeight,
				hint("Automatic: the deck starts and ends at the height of the ground at your first and last point, "
						+ "so it connects to the banks. Otherwise the whole deck gets this fixed height."),
				pathSlider("Arch height", " units", 0, 800, 10, Options.bridgeArc),
				hint("How far the middle of the bridge lies above a straight line between its ends. 0 gives a straight "
						+ "(or sloping) deck, higher values give an arched bridge."),
				pathSlider("Ramp width", " tiles", 0, 10, 1, Options.bridgeRamp),
				hint("Around the deck the terrain slopes down to the ground over this many tiles. 0 gives a hard edge."),
				flag);
		box.setPadding(new Insets(10));

		ScrollPane scroll = new ScrollPane(box);
		scroll.setFitToWidth(true);
		scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scroll.setPrefWidth(0);
		Tab tab = new Tab("Bridge");
		tab.setContent(scroll);
		return tab;
	}

	/** Settings of the mountain generator. */
	private Tab createMountainTab() {
		VBox box = new VBox(14);
		box.setPrefWidth(0);
		box.setPadding(new Insets(10));

		Label intro = hint("Pick the Mountain tool. Hill: click the centre of the mountain; with tiles selected, "
				+ "press Enter to raise the selection instead. Range: click points along the ridge, Enter applies, "
				+ "Esc cancels. The terrain is raised on top of what is already there; Ctrl+Z undoes it.");

		ToggleGroup modes = new ToggleGroup();
		RadioButton hill = new RadioButton("Hill / mountain");
		RadioButton range = new RadioButton("Mountain range");
		hill.setToggleGroup(modes);
		range.setToggleGroup(modes);
		range.setSelected(Options.mountainRange.get());
		hill.setSelected(!Options.mountainRange.get());
		range.selectedProperty().bindBidirectional(Options.mountainRange);
		VBox mode = section("Type", hill, range);

		VBox size = pathSlider("Radius", " tiles", 2, 60, 1, Options.mountainSize);
		size.disableProperty().bind(Options.mountainRange);
		VBox width = pathSlider("Width", " tiles", 2, 60, 1, Options.mountainSize);
		width.disableProperty().bind(Options.mountainRange.not());
		VBox peaks = pathSlider("Peak variation", "", 0, 1, 0.1, Options.mountainPeakVariation);
		peaks.disableProperty().bind(Options.mountainRange.not());

		VBox shape = section("Shape",
				pathSlider("Height", " units", 50, 3000, 50, Options.mountainHeight),
				hint("Height of the top. 128 units is one tile."),
				size, width,
				hint("Hill: radius of the mountain. Range: total width of the ridge."),
				pathSlider("Steepness", "", 1, 4, 0.5, Options.mountainSteepness),
				hint("1 is a gentle bell, higher gives steeper sides and a sharper top."),
				pathSlider("Foot blend", " tiles", 0, 20, 1, Options.mountainBlend),
				hint("Extra tiles around the mountain over which its foot fades into the surrounding terrain."));

		CheckBox cliff = new CheckBox("Cliff edge");
		cliff.selectedProperty().bindBidirectional(Options.mountainCliff);
		VBox cliffLevel = pathSlider("Cliff height", " of the top", 0.1, 0.9, 0.1, Options.mountainCliffLevel);
		cliffLevel.disableProperty().bind(Options.mountainCliff.not());
		VBox cliffWidth = pathSlider("Cliff width", " tiles", 1, 8, 1, Options.mountainCliffWidth);
		cliffWidth.disableProperty().bind(Options.mountainCliff.not());
		VBox cliffs = section("Cliffs",
				cliff,
				hint("A steep rock face along the edge of the mountain, with a gentler slope above it up to the top."),
				cliffLevel,
				hint("How much of the total height the cliff face itself climbs."),
				cliffWidth,
				hint("How many tiles the cliff face spans sideways: smaller is steeper."));

		VBox looks = section("Natural look",
				pathSlider("Irregularity", "", 0, 1, 0.1, Options.mountainIrregularity),
				hint("0 is a perfectly round outline, higher makes the outline uneven."),
				pathSlider("Roughness", " octaves", 1, 6, 1, Options.mountainOctaves),
				hint("1 is smooth, higher adds rocky detail on the slopes."),
				peaks,
				hint("Range only: how much the top height varies along the ridge, giving peaks and saddles."));

		Button newSeed = new Button("New seed");
		newSeed.setOnAction(evt -> Options.mountainSeed.set((int) (Math.random() * 1_000_000)));
		Label seedLabel = new Label();
		seedLabel.textProperty().bind(javafx.beans.binding.Bindings.format("Seed: %d", Options.mountainSeed));
		VBox seed = section("Seed", seedLabel, newSeed,
				hint("The same seed and settings always give the same mountain."));

		VBox footprint = createMountainShapeEditor();
		footprint.visibleProperty().bind(Options.mountainRange.not());
		footprint.managedProperty().bind(Options.mountainRange.not());

		box.getChildren().addAll(intro, mode, footprint, shape, cliffs, looks, seed);

		ScrollPane scroll = new ScrollPane(box);
		scroll.setFitToWidth(true);
		scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		scroll.setPrefWidth(0);
		Tab tab = new Tab("Mountain");
		tab.setContent(scroll);
		return tab;
	}

	/**
	 * Small grid in which the footprint of a hill is drawn: click or drag to colour cells, dragging from a coloured
	 * cell removes. The grid is stretched over the radius, so a bigger radius gives a bigger version of the drawing.
	 */
	private VBox createMountainShapeEditor() {
		final int grid = MountainGenerator.SHAPE_GRID;
		final double cell = 11;
		Canvas canvas = new Canvas(grid * cell, grid * cell);
		Runnable redraw = () -> {
			GraphicsContext g = canvas.getGraphicsContext2D();
			g.setFill(Color.web("#262626"));
			g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
			for (int x = 0; x < grid; x++) {
				for (int y = 0; y < grid; y++) {
					g.setFill(Options.mountainShape[x][y] ? Color.web("#7a9e5c") : Color.web("#333333"));
					g.fillRect(x * cell + 0.5, y * cell + 0.5, cell - 1, cell - 1);
				}
			}
		};
		redraw.run();

		boolean[] paint = new boolean[1];
		canvas.setOnMousePressed(evt -> {
			int x = (int) (evt.getX() / cell), y = (int) (evt.getY() / cell);
			if (x < 0 || y < 0 || x >= grid || y >= grid)
				return;
			paint[0] = !Options.mountainShape[x][y];
			Options.mountainShape[x][y] = paint[0];
			redraw.run();
		});
		canvas.setOnMouseDragged(evt -> {
			int x = (int) (evt.getX() / cell), y = (int) (evt.getY() / cell);
			if (x < 0 || y < 0 || x >= grid || y >= grid || Options.mountainShape[x][y] == paint[0])
				return;
			Options.mountainShape[x][y] = paint[0];
			redraw.run();
		});

		Button suggest = new Button("New suggestion");
		suggest.setOnAction(evt -> {
			Options.mountainShape = MountainGenerator.suggestShape((long) (Math.random() * 1_000_000),
					Options.mountainIrregularity.get());
			redraw.run();
		});
		Button clear = new Button("Clear");
		clear.setOnAction(evt -> {
			Options.mountainShape = new boolean[grid][grid];
			redraw.run();
		});
		Button fill = new Button("Fill");
		fill.setOnAction(evt -> {
			boolean[][] all = new boolean[grid][grid];
			for (boolean[] column : all)
				java.util.Arrays.fill(column, true);
			Options.mountainShape = all;
			redraw.run();
		});
		HBox buttons = new HBox(6, suggest, clear, fill);

		return section("Footprint",
				hint("Draw the outline of the mountain: click or drag to colour squares, drag from a coloured square "
						+ "to erase. North is up. The drawing is stretched over the radius. \"New suggestion\" makes "
						+ "a fresh uneven shape using the irregularity below."),
				canvas, buttons);
	}

	private static Label hint(String text) {
		Label label = new Label(text);
		label.setWrapText(true);
		label.setOpacity(0.7);
		label.setStyle("-fx-font-size: 11px;");
		return label;
	}

	private static VBox section(String title, Node... children) {
		Label header = new Label(title);
		header.setStyle("-fx-font-weight: bold;");
		VBox box = new VBox(6, header);
		box.getChildren().addAll(children);
		return box;
	}

	/** A slider that always moves in fixed steps and shows its current value. */
	private static VBox pathSlider(String name, String unit, double min, double max, double step,
	                               DoubleProperty property) {
		Slider slider = new Slider(min, max, property.get());
		slider.setBlockIncrement(step);
		slider.setMajorTickUnit(step);
		slider.setMinorTickCount(0);
		slider.setSnapToTicks(true);
		slider.valueProperty().bindBidirectional(property);
		Label label = new Label();
		String format = step >= 1 ? "%.0f" : "%.1f";
		label.textProperty().bind(javafx.beans.binding.Bindings.format(name + ": " + format + unit, property));
		return new VBox(4, label, slider);
	}


	/** Lets the user drag the left edge of the right hand tab panel to change its width (default 300px). */
	private void initRightPanelResize() {
		Pane parent = (Pane) rightPanel.getParent();
		Region handle = new Region();
		handle.setPrefWidth(5);
		handle.setMinWidth(5);
		handle.setMaxWidth(5);
		handle.setCursor(Cursor.H_RESIZE);
		handle.setStyle("-fx-background-color: rgba(128, 128, 128, 0.25);");
		double[] drag = new double[2];
		handle.setOnMousePressed(evt -> {
			drag[0] = evt.getScreenX();
			drag[1] = rightPanel.getWidth();
		});
		handle.setOnMouseDragged(evt -> {
			double width = drag[1] - (evt.getScreenX() - drag[0]);
			rightPanel.setPrefWidth(Math.max(250, Math.min(900, width)));
		});
		parent.getChildren().add(parent.getChildren().indexOf(rightPanel), handle);
	}

	public void onLoad(MainWindow application) {
		initializeToolButtons();
		initRightPanelResize();
		loadTabs(application);

		ChangeListenerUtil.addListener(() -> this.tileHeightTextBox.setText(this.heightLevelSlider.getValue() + ""),
				Options.tileHeightLevel);

		currentHeightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3, 0));
		windowControls = WindowControls.addWindowControls(application.getStage(), grabBar, controlBox);

		// windowControls.getResizeHelper().setMinWidth(1240);
		objectSelectionType.getItems().add("ALL");
		objectSelectionType.getSelectionModel().select(0);
		IntStream.range(0, 23).forEach(act -> objectSelectionType.getItems().add(String.valueOf(act)));

		Options.objectSelectionType.bind(objectSelectionType.getSelectionModel().selectedIndexProperty());

		brushTypeSelection.getItems().addAll(BrushType.values());
		Options.brushType.bindBidirectional(brushTypeSelection.valueProperty());
		// brushTypeSelection.get
		// brushTypeSelection.
		brushTypeSelection.getSelectionModel().select(0);

		ChangeListenerUtil.addListener(() -> SceneGraph.minimapUpdate = true, mapPane.widthProperty());

		Options.brushSize.bindBidirectional(brushSizeSlider.valueProperty());
		brushSizeTooltip.textProperty().set(Options.brushSize.get() + 1 + "");
		ChangeListenerUtil.addListener(() -> brushSizeTooltip.textProperty().set(Options.brushSize.get() + 1 + ""),
				Options.brushSize);

		Options.showDebug.bindBidirectional(showFPSCheckItem.selectedProperty());
		Options.showTileInformation.bindBidirectional(showTileDataCheckItem.selectedProperty());

		Options.tileHeightLevel.bindBidirectional(heightLevelSlider.valueProperty());
		// ChangeListenerUtil.addListener(() ->
		// heightLevelTooltip.setText(Options.tileHeightLevel.get() + ""),
		// Options.tileHeightLevel);

		Options.absoluteHeightProperty.bindBidirectional(this.absoluteHeightCheck.selectedProperty());
		Options.disableBlending.bindBidirectional(this.disableBlendingCheckItem.selectedProperty());
		Options.showObjects.bindBidirectional(this.showObjectsCheckItem.selectedProperty());
		Options.allHeightsVisible.bindBidirectional(this.allHeightsCheckItem.selectedProperty());
		Options.showOverlay.bindBidirectional(this.showOverlaysCheckItem.selectedProperty());

		Options.showOverlayNumbers.bindBidirectional(this.displayOverlayIds.selectedProperty());
		Options.showUnderlayNumbers.bindBidirectional(this.displayUnderlayIds.selectedProperty());
		Options.showTileHeightNumbers.bindBidirectional(this.displayTileHeights.selectedProperty());

		Options.showBlockedFlag.bindBidirectional(this.showBlockedFlag.selectedProperty());
		Options.showBridgeFlag.bindBidirectional(this.showBridgeFlag.selectedProperty());

		Options.showForceLowestPlaneFlag.bindBidirectional(this.showLowestFlag.selectedProperty());
		Options.showDisableRenderFlag.bindBidirectional(this.showDisableFlag.selectedProperty());
		Options.showLowerZFlag.bindBidirectional(this.showLowerZFlag.selectedProperty());

		Options.showMinimapFunctionModels.bindBidirectional(this.showMapIconObjs.selectedProperty());
		Options.loadAnimations.bindBidirectional(this.showAnimsMenuItem.selectedProperty());

		boolean remeberSize = (Boolean) Settings.properties.getOrDefault("remember_size",true);
		boolean remeberLocation = (Boolean) Settings.properties.getOrDefault("remember_location",false);
		boolean saveGroupName = (Boolean) Settings.properties.getOrDefault("save_group_name",false);

		ChangeListenerUtil.addListener(() -> {
			if(remeberSize) {
				Settings.properties.put("remember_size", false);
			} else {
				Settings.properties.put("remember_size", true);
			}
			rememberSize.setSelected(remeberSize);
			Settings.saveSettings();
		}, Options.rememberEditorSize);

		ChangeListenerUtil.addListener(() -> {
			if(remeberLocation) {
				Settings.properties.put("remember_location", false);
			} else {
				Settings.properties.put("remember_location", true);
			}
			rememberLocation.setSelected(remeberLocation);
			Settings.saveSettings();
		}, Options.rememberEditorLocation);


		ChangeListenerUtil.addListener(() -> {
			if(saveGroupName) {
				Settings.properties.put("save_group_name", false);
			} else {
				Settings.properties.put("save_group_name", true);
			}
			saveByGroupName.setSelected(saveGroupName);
			Settings.saveSettings();
		}, Options.saveByGroupName);

		rememberSize.setSelected(remeberSize);
		rememberLocation.setSelected(remeberLocation);
		saveByGroupName.setSelected(saveGroupName);

		Options.rememberEditorSize.bindBidirectional(this.rememberSize.selectedProperty());
		Options.rememberEditorLocation.bindBidirectional(this.rememberLocation.selectedProperty());
		Options.saveByGroupName.bindBidirectional(this.saveByGroupName.selectedProperty());

		decreaseBrushSizeBtn.setOnAction(act -> brushSizeSlider.adjustValue(brushSizeSlider.getValue() - 1));
		increaseBrushSizeBtn.setOnAction(act -> brushSizeSlider.adjustValue(brushSizeSlider.getValue() + 1));

		Runnable calculateFlags = () -> {
			BitFlag flag = new BitFlag();
			if (this.unwalkableCheck.isSelected())
				flag.flag(RenderFlags.BLOCKED_TILE);
			if (this.bridgeCheck.isSelected())
				flag.flag(RenderFlags.BRIDGE_TILE);
			if (this.forceLowestCheck.isSelected())
				flag.flag(RenderFlags.FORCE_LOWEST_PLANE);
			if (this.drawOnLowerZCheck.isSelected())
				flag.flag(RenderFlags.RENDER_ON_LOWER_Z);
			if (this.disableRenderCheck.isSelected())
				flag.flag(RenderFlags.DISABLE_RENDERING);

			Options.tileFlags.set(flag);
		};
		this.heightLevelSlider.valueProperty()
				.addListener((observable, oldVal, newVal) -> this.tileHeightTextBox.setText(newVal.intValue() + ""));
		this.tileHeightTextButton.setOnAction(
				evt -> { 
					this.heightLevelSlider.adjustValue(Doubles.tryParse(this.tileHeightTextBox.getText()));
					System.out.println(this.heightLevelSlider.valueProperty().get() + " : " + this.heightLevelSlider.valueProperty().intValue());
					this.getTileHeightTextBox().textProperty().set(this.getHeightLevelSlider().valueProperty().intValue() + "");
					this.tileHeightTextButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("notset"), false);
				});
		UnaryOperator<TextFormatter.Change> integerFilter = change -> {
			String newText = change.getControlNewText();
			
			if (newText.matches("-?([0-9]*)?")) {
				return change;
			}
			return null;
		};
		this.tileHeightTextBox.textProperty().addListener((observable, oldVal, newVal) -> {
			int val = Integer.parseInt(newVal);
			if (val != heightLevelSlider.valueProperty().intValue()) {
				this.tileHeightTextButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("notset"), true);
				System.out.println(val + " : " + this.heightLevelSlider.valueProperty().get());
			} else {
				this.tileHeightTextButton.pseudoClassStateChanged(PseudoClass.getPseudoClass("notset"), false);
			}
		});
		tileHeightTextBox.setTextFormatter(new TextFormatter<Integer>(new IntegerStringConverter(),
				heightLevelSlider.valueProperty().intValue(), integerFilter));
		tileHeightTextBox.addEventFilter(KeyEvent.ANY, evt -> {
			if(evt.getCode() == KeyCode.ENTER) {
				tileHeightTextButton.fire();
			}
		});

		ChangeListenerUtil.addListener(calculateFlags, unwalkableCheck.selectedProperty(),
				bridgeCheck.selectedProperty(), forceLowestCheck.selectedProperty(),
				drawOnLowerZCheck.selectedProperty(), disableRenderCheck.selectedProperty());

		Options.showHiddenTiles.bindBidirectional(hiddenTilesCheckItem.selectedProperty());
		Options.currentHeight.bind(currentHeightSpinner.valueProperty());

		Options.simulateBridgesProperty.bindBidirectional(simulateBridges.selectedProperty());
		
		Options.currentTool.addListener((observable, oldVal, newVal) -> {

			deselectTools();
			if(Client.getSingleton() != null && Client.getSingleton().sceneGraph != null) {
				Client.getSingleton().sceneGraph.resetTiles();
				Client.getSingleton().sceneGraph.resetLastHighlightedTiles();
			}
			
			/*if (oldVal == ToolType.PAINT_OVERLAY) {
				Options.overlayPaintId.set(-1);
				application.overlaySwatch.deselect();
			} else if (oldVal == ToolType.PAINT_UNDERLAY) {
				Options.underlayPaintId.set(-1);
				application.underlaySwatch.deselect();
			} else if (oldVal == ToolType.SPAWN_OBJECT) {
				Options.currentObject.set(null);
				application.objectSwatch.deselect();
			}*/

			if (newVal == ToolType.SELECT_OBJECT) {
				this.selectObjectBtn.setSelected(true);
			} else if (newVal == ToolType.DELETE_OBJECT) {
				this.deleteObjectBtn.setSelected(true);
			} else if (newVal == ToolType.MODIFY_HEIGHT) {
				this.heightModifyBtn.setSelected(true);
			} else if (newVal == ToolType.SET_FLAGS) {
				this.setFlagBtn.setSelected(true);
			} else if (newVal == ToolType.SELECT_TILE) {
				this.selectTileBtn.setSelected(true);
			} else if(newVal == ToolType.PAINT_OVERLAY) {
				this.paintOverlayBtn.setSelected(true);
			} else if(newVal == ToolType.PAINT_PATH) {
				this.paintPathBtn.setSelected(true);
			} else if(newVal == ToolType.MOUNTAIN) {
				this.mountainBtn.setSelected(true);
				if (mountainTab != null)
					toolsTabPane.getSelectionModel().select(mountainTab);
			} else if(newVal == ToolType.COAST) {
				this.coastBtn.setSelected(true);
				if (coastTab != null)
					toolsTabPane.getSelectionModel().select(coastTab);
			} else if(newVal == ToolType.BRIDGE) {
				this.bridgeBtn.setSelected(true);
				if (bridgeTab != null)
					toolsTabPane.getSelectionModel().select(bridgeTab);
			} else if(newVal == ToolType.PAINT_UNDERLAY) {
				this.paintUnderlayBtn.setSelected(true);
			}

		});

		this.setRelativeHeight.setOnAction(evt -> Client.getSingleton().sceneGraph.setAbsoluteHeight());
		this.setFlagsToTiles.setOnAction(
				evt -> SceneGraph.onCycleEnd.add(() -> Client.getSingleton().sceneGraph.setSelectedFlags()));
		this.setHeightsToTiles.setOnAction(
				evt -> SceneGraph.onCycleEnd.add(() -> Client.getSingleton().sceneGraph.setSelectedHeight()));
		this.setOverlays.setOnAction(
				evt -> SceneGraph.onCycleEnd.add(() -> Client.getSingleton().sceneGraph.setSelectedOverlays()));
		this.setUnderlays.setOnAction(
				evt -> SceneGraph.onCycleEnd.add(() -> Client.getSingleton().sceneGraph.setSelectedUnderlays()));


	}

	private void deselectTools() {
		toolGroup.getProperties().put("deselect", true);
		toolGroup.selectToggle(null);
	}

	private WindowControls windowControls;

}
