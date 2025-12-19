package org.infpls.noxio.game.module.game.util;

import java.util.*;
import org.infpls.noxio.game.module.game.game.object.GameObject;
import org.infpls.noxio.game.module.game.game.object.Vec2;

public class SpatialGrid {
    private final float cellSize;
    private final Map<Long, List<GameObject>> grid;

    public SpatialGrid(float cellSize) {
        this.cellSize = cellSize;
        this.grid = new HashMap<>();
    }

    public void clear() {
        grid.clear();
    }

    public void add(GameObject obj) {
        Vec2 pos = obj.getPosition();
        long key = getHash(pos.x, pos.y);
        grid.computeIfAbsent(key, k -> new ArrayList<>()).add(obj);
    }

    public List<GameObject> getNearby(Vec2 pos, float radius) {
        List<GameObject> nearby = new ArrayList<>();
        int minX = (int) Math.floor((pos.x - radius) / cellSize);
        int maxX = (int) Math.floor((pos.x + radius) / cellSize);
        int minY = (int) Math.floor((pos.y - radius) / cellSize);
        int maxY = (int) Math.floor((pos.y + radius) / cellSize);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                List<GameObject> cell = grid.get(getHashFromIndices(x, y));
                if (cell != null) {
                    nearby.addAll(cell);
                }
            }
        }
        return nearby;
    }

    private long getHash(float x, float y) {
        return getHashFromIndices((int) Math.floor(x / cellSize), (int) Math.floor(y / cellSize));
    }

    private long getHashFromIndices(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }
}
