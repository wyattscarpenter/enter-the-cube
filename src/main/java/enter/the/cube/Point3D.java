package enter.the.cube;

public class Point3D { //note that this class is also used for vector math
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

	public void set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void set(Point3D p) {
		this.x = p.x;
		this.y = p.y;
		this.z = p.z;
	}

	//vector/point math stuff

	//note that all of these are in-place
	//though we could easily make them new-point

	//note: maybe the point p versions should do the trivial re-implementation
	//for code reuse/correctness reasons

	//note: I have not implemented scalar add, etc. just do .add(a,a,a)
	public void add(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
	}

	public void add(Point3D p) {
		this.x = p.x;
		this.y = p.y;
		this.z = p.z;
	}

	public void subtract(double x, double y, double z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
	}

	public void subtract(Point3D p) {
		this.x -= p.x;
		this.y -= p.y;
		this.z -= p.z;
	}

	public void multiply(double x, double y, double z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
	}

	public void multiply(Point3D p) {
		this.x *= p.x;
		this.y *= p.y;
		this.z *= p.z;
	}

	public void divide(double x, double y, double z) {
		this.x /= x;
		this.y /= y;
		this.z /= z;
	}

	public void divide(Point3D p) {
		this.x /= p.x;
		this.y /= p.y;
		this.z /= p.z;
	}

	public double dot(double x, double y, double z) {
		return this.x*x+this.y*y+this.z*z;
	}

	public double dot(Point3D p) {
		return this.x*p.x+this.y*p.y+this.z*p.z;
	}

	public Point3D cross(double x, double y, double z) {
		return new Point3D(this.y*z-this.z*y, this.z*x-this.x*z, this.x*y-this.y*x);
	}

	public Point3D cross(Point3D p) {
		return cross(p.x, p.y, p.z);
	}
}
