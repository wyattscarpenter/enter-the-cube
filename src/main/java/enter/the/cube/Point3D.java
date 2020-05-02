package enter.the.cube;

public class Point3D { //note that this class is also used for vector math
	public static final Point3D zero = new Point3D(0,0,0);

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

	public Point3D(Point3D p) {
		this.x = p.x;
		this.y = p.y;
		this.z = p.z;
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
	//though we could easily make them new-point.
	//based on the sort of code I've been trying to write, I am almost sure it should have been new-point.

	//note: maybe the point p versions should do the trivial re-implementation
	//for code reuse/correctness reasons

	//note: I have not implemented scalar add, etc. just do .add(a,a,a)
	public Point3D add(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Point3D add(Point3D p) {
		return add(p.x, p.y, p.z);
	}

	public void subtract(double x, double y, double z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
	}

	public Point3D subtract(Point3D p) {
		this.x -= p.x;
		this.y -= p.y;
		this.z -= p.z;
		return this;
	}

	public Point3D multiply(double x, double y, double z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		return this;
	}

	public Point3D multiply(Point3D p) {
		return multiply(p.x, p.y, p.z);
	}

	public Point3D multiply(double scalar) {
		return multiply(scalar, scalar, scalar);
	}

	public void divide(double x, double y, double z) {
		this.x /= x;
		this.y /= y;
		this.z /= z;
	}

	public void divide(Point3D p) {
		divide(p.x, p.y, p.z);
	}

	public void divide(double scalar) {
		divide(scalar, scalar, scalar);
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

	public double magnitude() {
		return distance(zero);
	}

	public double distance(Point3D p) {
		return Math.sqrt(Math.pow(this.x-p.x,2)+Math.pow(this.y-p.y,2)+Math.pow(this.z-p.z,2));
	}

	public Point3D unit() {
		return new Point3D(x/magnitude(),y/magnitude(),z/magnitude());
	}

	public Point3D perp() {
		//from https://math.stackexchange.com/a/3582461
		//Thanks Danvil
		//(note: "user contributions licensed under cc by-sa 4.0 with attribution required.")
		double s = Math.signum(z) * this.magnitude();
		return new Point3D(s*(z+s)-x*x,-x*y,-x*(z+s));
	}

	public Point3D leftPerp2D() { //around z (this doesn't always make sense)
		// remember that the left perp of a vector <x,y> is <-y,x>
		return new Point3D(-y, x, z);
	}

	public Point3D rightPerp2D() { //around z (this doesn't always make sense)
		// remember that the left perp of a vector <x,y> is <-y,x>
		return new Point3D(y, -x, z);
	}

	public String toString() {
		return "("+x+", "+y+", "+z+")";

	}
}
