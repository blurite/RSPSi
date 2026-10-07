package com.rspsi.options;

import java.util.List;

import com.google.common.collect.Lists;
import com.jagex.map.SceneTileData;
import com.jagex.util.BitFlag;
import com.rspsi.datasets.ObjectDataset;
import com.rspsi.core.misc.BrushType;
import com.rspsi.core.misc.ToolType;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Options {

	public static BooleanProperty showOverlayNumbers = new SimpleBooleanProperty(false);
	public static BooleanProperty showUnderlayNumbers = new SimpleBooleanProperty(false);
	public static BooleanProperty showTileHeightNumbers = new SimpleBooleanProperty(false);

	public static BooleanProperty showHiddenTiles = new SimpleBooleanProperty(false);
	public static BooleanProperty showObjects = new SimpleBooleanProperty(true);
	public static BooleanProperty disableBlending = new SimpleBooleanProperty(false);
	public static BooleanProperty showOverlay = new SimpleBooleanProperty(false);
	public static BooleanProperty allHeightsVisible = new SimpleBooleanProperty(false);

	public static BooleanProperty simulateBridgesProperty = new SimpleBooleanProperty(false);
	
	public static BooleanProperty absoluteHeightProperty = new SimpleBooleanProperty(false);

	public static BooleanProperty showBlockedFlag = new SimpleBooleanProperty(false);
	public static BooleanProperty showBridgeFlag = new SimpleBooleanProperty(false);
	public static BooleanProperty showForceLowestPlaneFlag = new SimpleBooleanProperty(false);
	public static BooleanProperty showDisableRenderFlag = new SimpleBooleanProperty(false);
	public static BooleanProperty showLowerZFlag = new SimpleBooleanProperty(false);
	
	public static BooleanProperty showMinimapFunctionModels = new SimpleBooleanProperty(false);

	public static BooleanProperty showDebug = new SimpleBooleanProperty(false);
	public static BooleanProperty showTileInformation = new SimpleBooleanProperty(false);

	public static IntegerProperty currentHeight = new SimpleIntegerProperty(0);
	public static IntegerProperty tileHeightLevel = new SimpleIntegerProperty(50);
	public static IntegerProperty brushSize = new SimpleIntegerProperty(1);
	public static IntegerProperty objectSelectionType = new SimpleIntegerProperty(0);
	
	public static ObjectProperty<BitFlag> tileFlags = new SimpleObjectProperty<BitFlag>(new BitFlag());
	
	public static IntegerProperty rotation = new SimpleIntegerProperty(0);

	public static ObjectProperty<ToolType> currentTool = new SimpleObjectProperty<ToolType>(ToolType.SELECT_TILE);

	public static ObjectProperty<ObjectDataset> currentObject = new SimpleObjectProperty<ObjectDataset>();

	public static IntegerProperty overlayPaintId = new SimpleIntegerProperty(0);
	public static IntegerProperty overlayPaintShapeId = new SimpleIntegerProperty(1);
	/** Settings of the overlay path tool. */
	public static DoubleProperty pathWidth = new SimpleDoubleProperty(3);
	public static BooleanProperty pathSmoothCurve = new SimpleBooleanProperty(true);
	public static DoubleProperty pathEdgeSmoothing = new SimpleDoubleProperty(1.5);
	public static BooleanProperty pathSmoothHeight = new SimpleBooleanProperty(false);
	public static DoubleProperty pathHeightSmoothing = new SimpleDoubleProperty(5);
	/** How many tiles around the path are blended into the surrounding terrain when smoothing heights. */
	public static DoubleProperty pathHeightBlend = new SimpleDoubleProperty(4);
	/** Settings of the coast tool. */
	public static BooleanProperty coastCliff = new SimpleBooleanProperty(false);
	public static BooleanProperty coastSeaLeft = new SimpleBooleanProperty(true);
	public static DoubleProperty coastIrregularity = new SimpleDoubleProperty(2);
	public static DoubleProperty coastEdgeSmoothing = new SimpleDoubleProperty(2);
	public static IntegerProperty coastSeed = new SimpleIntegerProperty(1);
	public static DoubleProperty coastSeaWidth = new SimpleDoubleProperty(8);
	public static DoubleProperty coastBeachWidth = new SimpleDoubleProperty(24);
	public static DoubleProperty coastCliffHeight = new SimpleDoubleProperty(300);
	public static DoubleProperty coastCliffWidth = new SimpleDoubleProperty(2);
	public static DoubleProperty coastPlateauDepth = new SimpleDoubleProperty(10);

	/** Settings of the bridge tool. */
	public static DoubleProperty bridgeWidth = new SimpleDoubleProperty(3);
	public static DoubleProperty bridgeRamp = new SimpleDoubleProperty(1);
	public static DoubleProperty bridgeArc = new SimpleDoubleProperty(0);
	public static BooleanProperty bridgeAutoHeight = new SimpleBooleanProperty(true);
	public static DoubleProperty bridgeDeckHeight = new SimpleDoubleProperty(0);
	public static BooleanProperty bridgeFlag = new SimpleBooleanProperty(true);
	/** Settings of the mountain generator. */
	public static BooleanProperty mountainRange = new SimpleBooleanProperty(false);
	public static DoubleProperty mountainHeight = new SimpleDoubleProperty(600);
	public static DoubleProperty mountainSize = new SimpleDoubleProperty(12);
	public static DoubleProperty mountainOctaves = new SimpleDoubleProperty(3);
	public static DoubleProperty mountainIrregularity = new SimpleDoubleProperty(0.4);
	public static DoubleProperty mountainBlend = new SimpleDoubleProperty(3);
	public static DoubleProperty mountainSteepness = new SimpleDoubleProperty(1.5);
	public static DoubleProperty mountainPeakVariation = new SimpleDoubleProperty(0.5);
	public static BooleanProperty mountainCliff = new SimpleBooleanProperty(false);
	public static DoubleProperty mountainCliffLevel = new SimpleDoubleProperty(0.6);
	public static DoubleProperty mountainCliffWidth = new SimpleDoubleProperty(2);
	public static IntegerProperty mountainSeed = new SimpleIntegerProperty(1);
	/** Hand drawn footprint of a hill, see MountainGenerator.suggestShape; [column][row], row 0 is the top. */
	public static boolean[][] mountainShape = com.rspsi.tools.MountainGenerator.suggestShape(1, 0.4);
	public static IntegerProperty underlayPaintId = new SimpleIntegerProperty(0);
	public static ObjectProperty<BrushType> brushType = new SimpleObjectProperty<BrushType>(BrushType.RECTANGLE);
	
	public static BooleanProperty hdTextures = new SimpleBooleanProperty(false);

	public static List<SceneTileData> importData = Lists.newArrayList();

	public static BooleanProperty hdMap = new SimpleBooleanProperty(false);
	
	public static BooleanProperty loadAnimations = new SimpleBooleanProperty(false);

	public static BooleanProperty unsavedChanges = new SimpleBooleanProperty(false);
	
	public static IntegerProperty renderDistance = new SimpleIntegerProperty(30);
	public static IntegerProperty mapRegionSize = new SimpleIntegerProperty(256);
	

	public static BooleanProperty showCamera = new SimpleBooleanProperty(false);
	public static BooleanProperty showBorders = new SimpleBooleanProperty(false);
	public static BooleanProperty showMapFileNames = new SimpleBooleanProperty(false);

	public static BooleanProperty rememberEditorSize = new SimpleBooleanProperty(true);
	public static BooleanProperty rememberEditorLocation = new SimpleBooleanProperty(false);

	public static BooleanProperty saveByGroupName = new SimpleBooleanProperty(false);

}
