package remote;

import java.awt.Color;
import java.awt.Shape;
import java.io.Serializable;

public class MyShape implements Serializable {
	private Shape s;
	private Color c;
	private boolean isFill;
	private float b;
	public MyShape(Shape s, Color c, boolean isFill, float b) {
		this.s=s;
		this.c=c;
		this.isFill=isFill;
		this.b= b;
	}
	public boolean getIsFill() {
		return isFill;
	}
	public Shape getShape() {
		return s;
	}
	public Color getColor() {
		return c;
	}
	public float getBasicStroke() {
		return b;
	}
}
