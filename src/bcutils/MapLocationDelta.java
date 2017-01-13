package bcutils;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

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

  public float dot(MapLocationDelta r2) {
    return x * r2.x + y * r2.y;
  }
  
  public float dot(float x2, float y2) {
    return x * x2 + y * y2;
  }

  /*
   * Returns the MapLocationDelta projected onto dir
   * 
   * If Direction can be represented by unit vector \hat{n}, and the current
   * MapLocationDelta is \vec{r}, then the result of this operation is
   * (\vec{r} \cdot \hat{n}) \hat{n}
   * 
   * @param dir: Direction to be projected onto
   */
  public void projectOnto(Direction dir) {
    float dx = dir.getDeltaX(1);
    float dy = dir.getDeltaY(1);
    float dotProd = dot(dx, dy);
    x = dx * dotProd;
    y = dy * dotProd;
  }

  public void add(MapLocationDelta r2) {
    x += r2.x;
    y += r2.y;
  }
  
  public MapLocation addTo(MapLocation l) {
    return new MapLocation(l.x + x, l.y + y);
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
