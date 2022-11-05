package org.infpls.noxio.game.module.game.game.object;

public final class Vec3 {
  public final float x, y, z;
  public Vec3(final float x, final float y, final float z) {
    this.x = x; this.y = y; this.z = z;
  }
  public Vec3(final float a) {
    this(a, a, a);
  }
  public Vec3() {
    this(0f, 0f, 0f);
  }
  
  public boolean isZero() { return magnitude() == 0.0f; }
  public boolean isNaN() { return Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z); }
  public Vec3 add(final Vec3 b) { return new Vec3(x + b.x, y + b.y, z + b.z); }
  public Vec3 subtract(final Vec3 b) { return new Vec3(x - b.x, y - b.y, z - b.z); }
  public Vec3 scale(final float s) { return new Vec3(x*s, y*s, z*s); }
  public float magnitude() { return (float)(Math.sqrt((x*x)+(y*y)+(z*z))); }
  public float distance(final Vec3 b) { return subtract(b).magnitude(); }
  public Vec3 normalize() { final float m = magnitude(); return Math.abs(m) != 0f ? new Vec3(x/m,y/m,z/m) : new Vec3(0f, 1f, 0f); }  /* Safe on potential NaN */
  public boolean equals(final Vec3 b) { return x == b.x && y == b.y && z == b.z; }
  public Vec3 copy() { return new Vec3(x, y, z); } /* @TODO: Does not need to exist since Vec3 is final */
  public Vec2 trunc() { return new Vec2(x, y); }
  
  @Override
  public String toString() { return x + "," + y + "," + z; }
  public void toString(final StringBuilder sb) { sb.append(x); sb.append(","); sb.append(y); sb.append(","); sb.append(z); }
}