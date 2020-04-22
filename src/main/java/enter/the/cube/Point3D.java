package enter.the.cube;

public class Point3D {
	public double x;
	public double y;
	public double z;

	public Point3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Point3D() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}

	public boolean equals(Point3D p) {
		return p.x == x && p.y == y && p.z == z;
	}

	public boolean closeEnough(Point3D p, double margin) {
		return Math.abs(p.x - x)<margin && Math.abs(p.y - y)<margin && Math.abs(p.z - z)<margin;
	}

	public boolean closeEnough(Point3D p) {
		return closeEnough(p, 10);
	}

}
