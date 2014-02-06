/*   Implementation of the chaos game to generate flame fractals
    Copyright (C) 2014  Roman Klapaukh

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.Random;

public abstract class Variation {
	public abstract void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand);	
	public String toString()
	{
		return this.getClass().getName().substring("variation$".length());
	}
	public static  class Linear extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}

	public static class Sinusoidal extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = Math.sin(p[0]);
			newp[1] = Math.sin(p[1]);
		}
	}

	public static class Spherical extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] *= (1.0 / (r * r));
			newp[1] *= (1.0 / (r * r));
		}
	}

	public static class Swirl extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0] * Math.sin(r * r) - p[0] * Math.cos(r * r);
			newp[1] = p[0] * Math.cos(r * r) - p[1] * Math.sin(r * r);

		}
	}

	public static class Horseshoe extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = 1 / r * ((p[0] - p[1]) * (p[0] + p[1]));
			newp[1] = 2 * p[0] * p[1];
		}
	}

	public static class Polar extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = theta / Math.PI;
			newp[1] = r - 1;
		}
	}

	public static class Handkerchief extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = r * Math.sin(theta + r);
			newp[1] = r * Math.cos(theta - r);
		}
	}

	public static class Heart extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = r * Math.sin(theta * r);
			newp[0] = -r * Math.cos(theta * r);
		}
	}

	public static class Disc extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double t = theta / Math.PI;
			newp[0] = t * Math.sin(Math.PI * r);
			newp[0] = t * Math.cos(Math.PI * r);
		}
	}

	public static class Spiral extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = (1 / r) * (Math.cos(theta) + Math.sin(r));
			newp[0] = (1 / r) * (Math.sin(theta) + Math.cos(r));
		}
	}

	public static class Hyperbolic extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = Math.sin(theta) / r;
			newp[1] = r * Math.cos(theta);
		}
	}

	public static class Diamond extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = Math.sin(theta) * Math.cos(r);
			newp[1] = Math.cos(theta) * Math.sin(r);
		}
	}

	public static class Ex extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double p0 = Math.sin(theta + r);
			double p1 = Math.cos(theta - r);
			p0 = p0 * p0 * p0;
			p1 = p1 * p1 * p1;
			newp[0] = r * (p0 + p1);
			newp[1] = r * (p0 - p1);
		}
	}

	public static class Julia extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double sqr = Math.sqrt(r);
			newp[0] = sqr * (Math.cos(theta / 2 + (rand.nextBoolean() ? 0 : Math.PI)));
			newp[1] = sqr * (Math.sin(theta / 2 + (rand.nextBoolean() ? 0 : Math.PI)));
		}
	}

	public static class Bent extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			if (p[0] >= 0 && p[1] >= 0) {
				newp[0] = p[0];
				newp[1] = p[1];
			} else if (p[0] < 0 && p[1] >= 0) {
				newp[0] = 2 * p[0];
				newp[1] = p[1];
			} else if (p[0] >= 0 && p[1] < 0) {
				newp[0] = p[0];
				newp[1] = p[1] / 2;
			} else {
				newp[0] = 2 * p[0];
				newp[1] = p[1] / 2;
			}
		}
	}

	public static class Waves extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0] + c[1] * Math.sin(p[1] / (c[2] * c[2]));
			newp[1] = p[1] + c[4] * Math.sin(p[0] / (c[5] * c[5]));
		}
	}

	public static class Fisheye extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double e = 2.0 / (r + 1.0);
			newp[0] = e * p[1];
			newp[1] = e * p[0];
		}
	}

	public static class Popcorn extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0] + c[2] * Math.sin(Math.tan(3 * p[1]));
			newp[1] = p[1] + c[5] * Math.sin(Math.tan(3 * p[0]));
		}
	}

	public static class Exponential extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double e = Math.exp(p[0] - 1);
			newp[0] = e * Math.cos(Math.PI * p[1]);
			newp[1] = e * Math.sin(Math.PI * p[1]);
		}
	}

	public static class Power extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double e = Math.pow(r, Math.sin(theta));
			newp[0] = e * Math.cos(theta);
			newp[1] = e * Math.sin(theta);
		}
	}

	public static class Cosine extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = Math.cos(Math.PI * p[0]) * Math.cosh(p[1]);
			newp[1] = -Math.sin(Math.PI * p[0]) * Math.sinh(p[1]);
		}
	}

	public static class Rings extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double e = ((r + c[2] * c[2]) % (2 * c[2] * c[2])) - c[2] * c[2] + r * (1 - c[2] * c[2]);
			newp[0] = e * Math.cos(theta);
			newp[0] = e * Math.sin(theta);
		}
	}

	public static class Fan extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double t = Math.PI * c[2] * c[2];
			if ((theta + c[5]) % t > t / 2) {
				newp[0] = r * Math.cos(theta - t / 2);
				newp[1] = r * Math.sin(theta - t / 2);
			} else {
				newp[0] = r * Math.cos(theta + t / 2);
				newp[1] = r * Math.sin(theta + t / 2);
			}
		}
	}

	public static class Blob extends Variation {
		private double high;
		private double low;
		private double waves;

		public Blob(double high, double low, double waves) {
			this.high = high;
			this.low = low;
			this.waves = waves;
		}

		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double e = r * (low + ((high - low) / 2) * Math.sin(waves * theta + 1));
			newp[0] = e * Math.cos(theta);
			newp[1] = e * Math.sin(theta);
		}
	}

	public static class PDJ extends Variation {
		private double p1, p2, p3, p4;

		public PDJ(double a, double b, double c, double d) {
			this.p1 = a;
			this.p2 = b;
			this.p3 = c;
			this.p4 = d;
		}

		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = Math.sin(p1 * p[1]) - Math.cos(p2 * p[0]);
			newp[1] = Math.sin(p3 * p[1]) - Math.cos(p4 * p[0]);
		}
	}

	public static class Fan2 extends Variation {
		private double p1, p2;

		public Fan2(double x, double y) {
			p2 = y;
			p1 = Math.PI * x * x;
		}

		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double t = theta + p2 - p1 * (Math.floor((2 * theta * p2) / p1));
			if (t > p1 / 2) {
				newp[0] = r * Math.sin(theta - p1 / 2);
				newp[1] = r * Math.cos(theta - p1 / 2);
			} else {
				newp[0] = r * Math.sin(theta + p1 / 2);
				newp[1] = r * Math.cos(theta + p1 / 2);
			}
		}
	}

	public static class Rings2 extends Variation {
		private double v;

		public Rings2(double val) {
			v = val * val;
		}

		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double t = r - 2 * v * (Math.floor((r + v) / (2 * v))) + r * (1 - v);
			newp[0] = t * Math.sin(theta);
			newp[1] = t * Math.cos(theta);
		}
	}

	public static class Eyefish extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double t = 2 / (r + 1);
			newp[0] = t * p[0];
			newp[1] = t * p[1];
		}
	}

	public static class Bubble extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double t = 4 / (r * r + 4);
			newp[0] = t * p[0];
			newp[1] = t * p[1];
		}
	}

	public static class Cylinder extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = Math.sin(p[0]);
			newp[1] = p[1];
		}
	}

	public static class Perspective extends Variation {
		double p1, p2;

		public Perspective(double angle, double dist) {
			p1 = angle;
			p2 = dist;
		}

		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double t = p2 / (p2 - p[0] * Math.sin(p1));
			newp[0] = t * p[0];
			newp[1] = t * p[1] * Math.cos(p1);
		}
	}

	public static class Noise extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double t1 = rand.nextDouble();
			double t2 = rand.nextDouble();
			newp[0] = t1 * p[0] * Math.cos(2 * Math.PI * t2);
			newp[1] = t1 * p[1] * Math.sin(2 * Math.PI * t2);
		}
	}

	public static class JuliaN extends Variation {
		double p1, p2;

		public JuliaN(double power, double dist) {
			p1 = power;
			p2 = dist;
		}

		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double p3 = Math.floor(Math.abs(p1) * rand.nextDouble());
			double t = (phi + 2 * Math.PI * p3) / p1;
			double f = Math.pow(r, p2 / p1);
			newp[0] = f * Math.cos(t);
			newp[1] = f * Math.sin(t);
		}
	}

	public static class JuliaScope extends Variation {
		double p1, p2;

		public JuliaScope(double power, double dist) {
			p1 = power;
			p2 = dist;
		}

		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double p3 = Math.floor(Math.abs(p1) * rand.nextDouble());
			double t = ((rand.nextBoolean() ? 1 : -1) * phi + 2 * Math.PI * p3) / p1;
			double f = Math.pow(r, p2 / p1);
			newp[0] = f * Math.cos(t);
			newp[1] = f * Math.sin(t);
		}
	}

	public static class Blur extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double psi1 = rand.nextDouble();
			double psi2 = rand.nextDouble();
			newp[0] = psi1 * Math.cos(2 * Math.PI * psi2);
			newp[1] = psi1 * Math.sin(2 * Math.PI * psi2);
		}
	}

	public static class Gaussian extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double psi1 = 0;
			for (int i = 0; i < 4; i++) {
				psi1 += rand.nextDouble() - 1;
			}
			double psi2 = rand.nextDouble();
			newp[0] = psi1 * Math.cos(2 * Math.PI * psi2);
			newp[1] = psi1 * Math.sin(2 * Math.PI * psi2);
		}
	}

	public static class RadialBlur extends Variation {
		double p1;
		double v;

		public RadialBlur(double angle, double v36) {
			p1 = angle * Math.PI / 2.0;
			v = v36;
		}

		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double t1 = 0;
			for (int i = 0; i < 4; i++) {
				t1 += rand.nextDouble() - 2;
			}
			t1 *= v;
			double t2 = phi + t1 * Math.sin(p1);
			double t3 = t1 * Math.cos(p1) - 1;
			double f = 1.0 / v;
			newp[0] = f * (r * Math.cos(t2) + t3 * p[0]);
			newp[1] = f * (r * Math.sin(t2) + t3 * p[1]);
		}
	}

	public static class Pie extends Variation {
		double p1, p2, p3;

		public Pie(int slices, double rotation, double thickness) {
			p1 = slices;
			p2 = rotation;
			p3 = thickness;
		}

		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double t1 = Math.floor(rand.nextDouble() * p1 + 0.5);
			double t2 = p2 + ((2 * Math.PI) / p1) * (t1 * rand.nextDouble() * p3);
			double psi3 = rand.nextDouble();
			newp[0] = psi3 * Math.cos(t2);
			newp[1] = psi3 * Math.sin(t2);
		}
	}

	public static class Ngon extends Variation {
		private double p1, p2, p3, p4;

		public Ngon(double power, int sides, int corners, double circle) {
			p1 = power;
			p2 = 2.0 * Math.PI / sides;
			p3 = corners;
			p4 = circle;
		}

		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double t3 = phi - p2 * Math.floor(phi / p2);
			double t4;
			if (t3 > p2 / 2) {
				t4 = t3;
			} else {
				t4 = t3 - p2;
			}
			double k = (p3 * ((1.0 / Math.cos(t4)) + 1) + p4) / (Math.pow(r, p1));
			newp[0] = k * p[0];
			newp[1] = k * p[1];
		}
	}

	public static class Curl extends Variation {
		private double p1, p2;

		public Curl(double c1, double c2) {
			this.p1 = c1;
			this.p2 = c2;
		}

		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double t1 = 1 + p1 * p[0] + p2 * (p[0] * p[0] - p[1] * p[1]);
			double t2 = p1 * p[1] + 2 * p2 * p[0] * p[1];
			double k = 1.0 / (t1 * t1 + t2 * t2);
			newp[0] = k * (p[0] * t1 + p[1] * t2);
			newp[1] = k * (p[1] * t1 - p[0] * t2);
		}
	}

	public static class Rectangles extends Variation {
		private double p1, p2;

		public Rectangles(double x, double y) {
			p1 = x;
			p2 = y;
		}

		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = (2 * Math.floor(p[0] / p1) + 1) * p1 - p[0];
			newp[1] = (2 * Math.floor(p[1] / p2) + 1) * p2 - p[1];
		}
	}

	public static class Arch extends Variation {
		private double v;

		public Arch(double v41) {
			this.v = v41;
		}

		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = Math.sin(rand.nextDouble() * Math.PI * v);
			newp[1] = Math.pow(Math.sin(rand.nextDouble() * Math.PI * v), 2) / Math.cos(rand.nextDouble() * Math.PI * v);
		}
	}

	public static class Tangent extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = Math.sin(p[0]) / Math.cos(p[1]);
			newp[1] = Math.tan(p[1]);
		}
	}

	public static class Square extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = rand.nextDouble() - 0.5;
			newp[1] = rand.nextDouble() - 0.5;
		}
	}

	public static class Rays extends Variation {
		double v;

		public Rays(double v44) {
			this.v = v44;
		}

		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double k = v * Math.tan(rand.nextDouble() * Math.PI * v) / (r * r);
			newp[0] = k * Math.cos(p[0]);
			newp[1] = k * Math.sin(p[1]);
		}
	}

	public static class Blade extends Variation {
		double v;

		public Blade(double v45) {
			this.v = v45;
		}

		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0] * (Math.cos(rand.nextDouble() * r * v) + Math.sin(rand.nextDouble() * r * v));
			newp[1] = p[0] * (Math.cos(rand.nextDouble() * r * v) - Math.sin(rand.nextDouble() * r * v));
		}
	}

	public static class Secant extends Variation {
		double v;

		public Secant(double v46) {
			this.v = v46;
		}

		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = 1.0/(v*Math.cos(v*r));
		}
	}

	public static class Twintrian extends Variation {
		double v;
		public Twintrian(double v47){
			this.v = v47;
		}
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double t = Math.log10(Math.pow(Math.sin(rand.nextDouble()*r*v),2)) + Math.cos(rand.nextDouble()*r*v);
			newp[0] = p[0]*t;
			newp[1] = p[0]*(t - Math.PI*Math.sin(rand.nextDouble()*r*v));
		}
	}

	public static class Cross extends Variation {
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double t = p[0]*p[0] + p[1]*p[1];
			t *=t;
			t = 1/t;
			t = Math.sqrt(t);
			
			newp[0] = t*p[0];
			newp[1] = t*p[1];
		}
	}
}
