import java.util.Random;


public interface Variation {
	public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand);
	
	public class Linear implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Sinusoidal implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = Math.sin(p[0]);
			newp[1] = Math.sin(p[1]);
		}
	}
	public class Spherical implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] *= (1.0/(r*r));
			newp[1] *= (1.0/(r*r));
		}
	}
	public class Swirl implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0]*Math.sin(r*r) - p[0]*Math.cos(r*r);
			newp[1] = p[0]*Math.cos(r*r) - p[1]*Math.sin(r*r);

		}
	}
	public class Horseshoe implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = 1/r*((p[0]-p[1])*(p[0]+p[1]));
			newp[1] = 2*p[0]*p[1];
		}
	}
	public class Polar implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = theta/ Math.PI;
			newp[1] = r -1;
		}
	}
	public class Handkerchief implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = r*Math.sin(theta+r);
			newp[1] = r * Math.cos(theta-r);
		}
	}
	public class Heart implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = r*Math.sin(theta*r);
			newp[0] = -r*Math.cos(theta*r);
		}
	}
	public class Disc implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double t= theta/Math.PI;
			newp[0] = t*Math.sin(Math.PI*r);
			newp[0] = t*Math.cos(Math.PI*r);
		}
	}
	public class Spiral implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = (1/r)*(Math.cos(theta) + Math.sin(r));
			newp[0] = (1/r)*(Math.sin(theta) + Math.cos(r));
		}
	}
	public class Hyperbolic implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = Math.sin(theta)/r;
			newp[1] = r*Math.cos(theta);
		}
	}
	public class Diamond implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = Math.sin(theta) * Math.cos(r);
			newp[1] = Math.cos(theta) * Math.sin(r);
		}
	}
	public class Ex implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double p0 = Math.sin(theta+r);
			double p1 = Math.cos(theta-r);
			p0 = p0*p0*p0;
			p1 = p1*p1*p1;
			newp[0] = r*(p0+p1);
			newp[1] = r*(p0-p1);
		}
	}
	public class Julia implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double sqr = Math.sqrt(r);
			newp[0] = sqr* (Math.cos(theta/2 + (rand.nextBoolean()?0:Math.PI)));
			newp[1] = sqr* (Math.sin(theta/2 + (rand.nextBoolean()?0:Math.PI)));
		}
	}
	public class Bent implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			if(p[0]>=0 && p[1] >=0){
				newp[0] = p[0];
				newp[1] = p[1];
			}else if( p[0] < 0 && p[1] >= 0 ){
				newp[0] = 2*p[0];
				newp[1] = p[1];
			}else if(p[0] >= 0 && p[1]< 0){
				newp[0] = p[0];
				newp[1] = p[1]/2;
			}else{
				newp[0] = 2*p[0];
				newp[1] = p[1]/2;
			}
		}
	}
	public class Waves implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0] + c[1]*Math.sin(p[1]/(c[2]*c[2]));
			newp[1] = p[1] + c[4]*Math.sin(p[0]/(c[5]*c[5]));
		}
	}
	public class Fisheye implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double e=2.0/(r+1.0);
			newp[0] = e* p[1];
			newp[1] = e* p[0];
		}
	}
	public class Popcorn implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0] + c[2]*Math.sin(Math.tan(3*p[1]));
			newp[1] = p[1] + c[5]*Math.sin(Math.tan(3*p[0]));
		}
	}
	public class Exponential implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double e = Math.exp(p[0]-1);
			newp[0] = e*Math.cos(Math.PI*p[1]);
			newp[1] = e*Math.sin(Math.PI*p[1]);
		}
	}
	public class Power implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double e = Math.pow(r,Math.sin(theta));
			newp[0] = e*Math.cos(theta);
			newp[1] = e*Math.sin(theta);
		}
	}
	public class Cosine implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0]= Math.cos(Math.PI*p[0])*Math.cosh(p[1]);
			newp[1]= -Math.sin(Math.PI*p[0])*Math.sinh(p[1]);
		}
	}
	public class Rings implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double e = ((r+c[2]*c[2])%(2*c[2]*c[2])) - c[2]*c[2] + r*(1-c[2]*c[2]);
			newp[0] = e*Math.cos(theta);
			newp[0] = e*Math.sin(theta);
		}
	}
	public class Fan implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double t = Math.PI*c[2]*c[2];
			if((theta+c[5]) %t > t/2){
				newp[0] = r*Math.cos(theta-t/2);
				newp[1] = r*Math.sin(theta-t/2);
			}else{
				newp[0] = r*Math.cos(theta+t/2);
				newp[1] = r*Math.sin(theta+t/2);
			}
		}
	}
	public class Blob implements Variation{
		private double high;
		private double low;
		private double waves;
		public Blob(double high,double low,double waves){
			this.high = high;
			this.low = low;
			this.waves = waves;
		}
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			double e = r * (low + ((high - low)/2) * Math.sin(waves*theta + 1));
			newp[0] = e*Math.cos(theta);
			newp[1] = e*Math.sin(theta); 
		}
	}
	public class PDJ implements Variation{
		private double p1,p2,p3,p4;
		public PDJ(double a, double b, double c, double d){
			this.p1=a;
			this.p2=b;
			this.p3=c;
			this.p4=d;
		}
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = Math.sin(p1*p[1]) - Math.cos(p2*p[0]);
			newp[1] = Math.sin(p3*p[1]) - Math.cos(p4*p[0]);
		}
	}
	public class Fan2 implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Rings2 implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Eyefish implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Bubble implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Cylinder implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Perspective implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Noise implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class JuliaN implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class JuliaScope implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Blur implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Gaussian implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class RadialBlur implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Pie implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Ngon implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Curl implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Rectangles implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Arch implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Tangent implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Square implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Rays implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Blade implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Secant implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Twintrian implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
	public class Cross implements Variation{
		public void f(double[] p, double[] newp, double[] c, double r, double theta, double phi, Random rand) {
			newp[0] = p[0];
			newp[1] = p[1];
		}
	}
}

