package com.rspsi.game.map;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.swing.JComponent;

import com.jagex.net.ResourceResponse;
import com.jagex.util.TextRenderUtils;

public class RegionView extends JComponent {

	/** Maximum number of regions that keep their rendered thumbnails; the rest are reloaded when scrolled back into view. */
	private static final int MAX_CACHED_VIEWS = 1500;

	/** Access-ordered, so the first entries are the least recently painted views. */
	private static final Map<RegionView, Boolean> cachedViews = new LinkedHashMap<>(256, 0.75f, true);

	private volatile MapTile region;
	private boolean isHovered;
	private boolean isSelected;
	private int landscapeId = -1;
	private int objectsId = -1;
	private int hash;
	private int regionX;
	private int regionY;
	public volatile BufferedImage[] images;

	/**
	 * Called by the tile once its thumbnails are rendered. Stale results (the tile was replaced or
	 * evicted in the meantime) are dropped.
	 */
	void setImages(MapTile tile, BufferedImage[] rendered) {
		if (region != tile)
			return;
		images = rendered;
		synchronized (cachedViews) {
			cachedViews.put(this, Boolean.TRUE);
			evictOldViews();
		}
		invalidate();
	}

	private static void evictOldViews() {
		Iterator<RegionView> it = cachedViews.keySet().iterator();
		while (cachedViews.size() > MAX_CACHED_VIEWS && it.hasNext()) {
			RegionView view = it.next();
			if (view.isOnScreen())
				continue;
			it.remove();
			view.images = null;
			view.region = null;
		}
	}

	private boolean isOnScreen() {
		return isShowing() && !getVisibleRect().isEmpty();
	}

	@Override
	public void invalidate(){
		super.invalidate();
		if(this.getParent() != null)
			this.getParent().repaint();
	}

	public void setHovered(boolean isHovered) {
		this.isHovered = isHovered;
		this.invalidate();
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
		this.invalidate();
	}

	public RegionView(int x, int y) {
		super();
		this.regionX = x;
		this.regionY = y;
		this.hash = (x << 8) + y;
	}
	
	


	public void loadMap(){
		Optional<MapTile> mapTile = MapTile.create(this, regionX, regionY);
		if(!mapTile.isPresent())
			return;
		MapTile tile = mapTile.get();
		this.region = tile;
		this.landscapeId = tile.landscapeId;
		this.objectsId = tile.objectsId;
	}

	@Override
	public void paintComponent(Graphics g){
		//System.out.println("painting component");
		//if(region.isVisible(minX, maxX, minY, maxY)){
			Graphics2D g2d = (Graphics2D) g;
			g.setFont(new Font("Helvetica", 0, 9));
			boolean exists = MapTile.exists(regionX, regionY);
			if(exists && region == null && images == null)
				loadMap();
			if(!exists){
				g.setColor(java.awt.Color.black);
				g.fillRect(0, 0, 64, 64);
				TextRenderUtils.renderCenter(g2d, "NULL", 32, 18, Color.red.getRGB());
				if(MapView.renderXY) {


					TextRenderUtils.renderCenter(g2d, "X: " + (regionX * 64), 31, 33, Color.red.getRGB());
					TextRenderUtils.renderCenter(g2d, "Y: " + (regionY * 64), 31, 43, Color.red.getRGB());
				}
			} else {
				if(!MapView.showImages.get()) {
					if(MapView.renderHash) {

						TextRenderUtils.renderCenter(g2d, "HASH: " + hash, 32, 18, Color.black.getRGB());
						TextRenderUtils.renderCenter(g2d, "HASH: " + hash, 31, 19, Color.white.getRGB());
					} 
					if(MapView.renderXY) {

						TextRenderUtils.renderCenter(g2d, "X: " + (regionX * 64), 32, 32, Color.black.getRGB());
						TextRenderUtils.renderCenter(g2d, "Y: " + (regionY * 64), 32, 42, Color.black.getRGB());

						TextRenderUtils.renderCenter(g2d, "X: " + (regionX * 64), 31, 33, Color.white.getRGB());
						TextRenderUtils.renderCenter(g2d, "Y: " + (regionY * 64), 31, 43, Color.white.getRGB());
					}
				} else if(images == null) {
					MapTile tile = region;
						if(tile != null)
							tile.loadTile();
					g.setColor(java.awt.Color.black);
					g.clearRect(0, 0, 64, 64);
					g.setColor(java.awt.Color.black);
					g.fillRect(0, 0, 64, 64);
					g.setColor(java.awt.Color.white);
					if(MapView.renderHash) 
						TextRenderUtils.renderCenter(g2d, "HASH: " + hash, 32, 18, Color.white.getRGB());

					TextRenderUtils.renderCenter(g2d, "Loading...", 32, 42, Color.white.getRGB());


				} else {
					region = null;
					synchronized (cachedViews) {
							cachedViews.get(this);//marks this view as recently used
						}
						BufferedImage[] shown = images;
						if(shown != null && shown[MapView.heightLevel.get()] != null)
							g.drawImage(shown[MapView.heightLevel.get()], 0, 0, this);
					if(MapView.renderHash) {

						TextRenderUtils.renderCenter(g2d, "HASH: " + hash, 32, 18, Color.black.getRGB());
						TextRenderUtils.renderCenter(g2d, "HASH: " + hash, 31, 19, Color.white.getRGB());
					} 
					if(MapView.renderXY) {

						TextRenderUtils.renderCenter(g2d, "X: " + (regionX * 64), 32, 32, Color.black.getRGB());
						TextRenderUtils.renderCenter(g2d, "Y: " + (regionY * 64), 32, 42, Color.black.getRGB());

						TextRenderUtils.renderCenter(g2d, "X: " + (regionX * 64), 31, 33, Color.white.getRGB());
						TextRenderUtils.renderCenter(g2d, "Y: " + (regionY * 64), 31, 43, Color.white.getRGB());
					}
				}
			}
			//}	

			if(this.isSelected){
				g.setColor(new java.awt.Color(104, 66, 244, 50));
				g.fillRect(0, 0, 64, 64);
			} else if(this.isHovered){
				g.setColor(new java.awt.Color(66, 134, 244, 50));
				g.fillRect(0, 0, 64, 64);
			}

			g.setColor(java.awt.Color.red);
			g.drawRect(0, 0, 64, 64);

	}

	public void deliverResource(ResourceResponse response) {
		MapTile tile = region;
		if(tile != null && tile.landscapeId == response.getRequest().getGroup()) {
			tile.onResourceResponse(response);
		}
	}

	public int getRegionX() {
		return regionX;
	}

	public void setRegionX(int regionX) {
		this.regionX = regionX;
	}

	public int getLandscapeId() {
		return landscapeId;
	}

	public int getObjectsId() {
		return objectsId;
	}

	public int getRegionY() {
		return regionY;
	}



}
