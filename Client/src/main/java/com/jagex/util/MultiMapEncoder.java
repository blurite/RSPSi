package com.jagex.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.jagex.chunk.Chunk;
import com.rspsi.core.misc.Vector2;

public class MultiMapEncoder {
	
	public static byte[] encode(List<Chunk> chunks) {
		return write(chunks, chunk -> chunk.scenegraph.saveObjects(chunk), chunk -> chunk.mapRegion.save_terrain_block(chunk));
	}

	public static byte[] encodeShallow(List<Chunk> chunks) {
		return write(chunks, chunk -> chunk.objectMapData, chunk -> chunk.tileMapData);
	}

	/**
	 * Writes every loaded chunk to a buffer that grows as needed, so packs of any size can be
	 * encoded (a fixed buffer overflowed for large packs).
	 */
	private static byte[] write(List<Chunk> chunks, Function<Chunk, byte[]> objects, Function<Chunk, byte[]> tiles) {
		List<Chunk> loaded = new ArrayList<>();
		for (Chunk chunk : chunks) {
			if (chunk.hasLoaded())
				loaded.add(chunk);
		}

		ByteArrayOutputStream bytes = new ByteArrayOutputStream(64 * 1024);
		DataOutputStream out = new DataOutputStream(bytes);
		try {
			out.writeInt(loaded.size());
			for (Chunk chunk : loaded) {
				byte[] objectMap = objects.apply(chunk);
				byte[] tileMap = tiles.apply(chunk);

				out.writeInt(chunk.objectMapId);
				out.writeInt(chunk.tileMapId);

				out.writeInt(chunk.offsetX / 64);
				out.writeInt(chunk.offsetY / 64);

				out.writeInt(objectMap.length);
				out.write(objectMap);

				out.writeInt(tileMap.length);
				out.write(tileMap);
			}
		} catch (IOException e) {
			// writing to a ByteArrayOutputStream cannot fail
			throw new UncheckedIOException(e);
		}
		return bytes.toByteArray();
	}

	public static Vector2 getSize(byte[] encoded) {
		ByteBuffer buffer = ByteBuffer.wrap(encoded);
		int size = buffer.getInt();

		int maximumX = 0;
		int maximumY = 0;
		for(int i = 0;i<size;i++) {
			buffer.getInt();
			buffer.getInt();

			int positionX = buffer.getInt();
			int positionY = buffer.getInt();

			if(positionX > maximumX)
				maximumX = positionX;
			if(positionY > maximumY)
				maximumY = positionY;

			buffer.get(new byte[buffer.getInt()]);
			buffer.get(new byte[buffer.getInt()]);

		}

		return new Vector2(maximumX, maximumY);
	}
	public static List<Chunk> decode(byte[] encoded){
		List<Chunk> chunks = Lists.newArrayList();
		ByteBuffer buffer = ByteBuffer.wrap(encoded);
		int size = buffer.getInt();
		
		for(int i = 0;i<size;i++) {
			int objectMapId = buffer.getInt();
			int tileMapId = buffer.getInt();
			
			int positionX = buffer.getInt();
			int positionY = buffer.getInt();
			
			int cX = (0 + 64 * positionX) / 64;
			int cY = (0 + 64 * positionY) / 64;

			int hash = (cX << 8) + cY;
			
			int objLen = buffer.getInt();
			byte[] objData = new byte[objLen];
			buffer.get(objData);
			
			int landscapeLen = buffer.getInt();
			byte[] landscapeData = new byte[landscapeLen];
			buffer.get(landscapeData);
			
			Chunk chunk = new Chunk(hash);

			chunk.offsetX = 64 * positionX;
			chunk.offsetY = 64 * positionY;
			
			chunk.objectMapData = objData;
			chunk.tileMapData = landscapeData;
			chunk.objectMapId = objectMapId;
			chunk.tileMapId = tileMapId;
			
			System.out.println("Loaded chunk " + chunk.offsetX + " : " + chunk.offsetY + " IDS: " + chunk.objectMapId + " : " + chunk.tileMapId);
			chunks.add(chunk);
		}
		return chunks;
	}

}
