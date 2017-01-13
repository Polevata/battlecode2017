package bcutils;

import battlecode.common.Direction;

public class MapLocationDelta {
  private float x, y;

  public MapLocationDelta(float x_, float y_) {
    x = x_;
    y = y_;
  }

  public MapLocationDelta(Direction dir, float dist) {
    x = dir.getDeltaX(dist);
    y = dir.getDeltaY(dist);
  }
  
  public float getX() {
    return x;
  }
  
  public float getY() {
    return y;
  }
  
  public float length() {
    return (float) Math.sqrt(x * x + y * y);
  }
  
  public float lengthSquared() {
    return x * x + y * y;
  }

  public void projectOnto(Direction dir) {
    x *= dir.getDeltaX(1);
    y *= dir.getDeltaY(2);
  }

  public void add(MapLocationDelta r2) {
    x += r2.x;
    y += r2.y;
  }
  
  public void scaleTo(float newLength) {
    float scale = newLength / length();
    x *= scale;
    y *= scale;
  }

  public void scaleBy(float scale) {
    x *= scale;
    y *= scale;
  }

}
