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
  public float magnitude() { return (float)(Math.sqrt((x*x)+(y*y)+(z*z))); }
  public boolean equals(final Vec3 b) { return x == b.x && y == b.y && z == b.z; }
  public Vec3 copy() { return new Vec3(x, y, z); } /* @TODO: Does not need to exist since Vec3 is final */
  
  @Override
  public String toString() { return x + "," + y + "," + z; }
  public void toString(final StringBuilder sb) { sb.append(x); sb.append(","); sb.append(y); sb.append(","); sb.append(z); }
}