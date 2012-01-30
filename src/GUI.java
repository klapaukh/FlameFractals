import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class GUI extends JComponent {

	private static final long serialVersionUID = -8900010760721989010L;
	double[][] coeffs;// = new double[][] { { 0.5, 0, 0, 0, 0.5, 0 }, { 0.5, 0, 0.5, 0, 0.5, 0 }, { 0.5, 0, 0, 0, 0.5, 0.5 }, { 1, 0, 0, 0, 1, 0 } };
	double[][] postCoeffs;// = new double[][] { { 1, 0, 0, 0, 1, 0 }, { 1, 0, 0, 0, 1, 0 }, { 1, 0, 0, 0, 1, 0 }, { 1, 0, 0, 0, 1, 0 } };
	double[][] funcColors;// = new double[][] { { 0, 1, 0 }, { 1, 0, 0 }, { 0, 0, 1 }, { 0.5, 0.5, 0.5 } };
	int numIter = 1000000;
	Random rand = new Random();
	Variation[] variations;
	double[] varWeights;
	int count;

	public GUI() {
		long seed = rand.nextLong();
		rand.setSeed(seed);
		System.out.println("Seed: " + seed);
		coeffs = new double[rand.nextInt(10) + 2][6];
		postCoeffs = new double[coeffs.length][6];
		funcColors = new double[coeffs.length][3];
		for (int i = 0; i < coeffs.length; i++) {
			for (int j = 0; j < 6; j++) {
				coeffs[i][j] = rand.nextGaussian();
				postCoeffs[i][j] = rand.nextGaussian();
				if (j < funcColors[i].length) {
					funcColors[i][j] = rand.nextDouble();
				}
			}
		}
//		for (int i = 0; i < coeffs.length; i++) {
//			System.out.printf("%.2fx + %.2fy + %.2f\n", coeffs[i][0], coeffs[i][1], coeffs[i][2]);
//			System.out.printf("%.2fx + %.2fy + %.2f\n\n", coeffs[i][3], coeffs[i][4], coeffs[i][5]);
//		}
		initVariations();
		JFrame frame = new JFrame("Chaos Game " + seed);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1024, 768);
		frame.getContentPane().add(this);
		frame.setVisible(true);
	}

	public static void main(String args[]) {
		new GUI();
	}

	public void paint(Graphics g) {
		count = 0;
		int width = this.getWidth();
		int height = this.getHeight();

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);

		int[][] data = new int[width][height];
		double[][][] colors = new double[width][height][4];

		double[] p = new double[] { rand.nextDouble(), rand.nextDouble() }, newp = new double[2], totalp = new double[2], col = new double[] {
				rand.nextDouble(), rand.nextDouble(), rand.nextDouble() };
		for (int i = 0; i < 20; i++) {
			int f = rand.nextInt(coeffs.length - 1);
			F(f, p, newp, totalp);
			col[0] = (col[0] + funcColors[f][0]) / 2.0;
			col[1] = (col[1] + funcColors[f][1]) / 2.0;
			col[2] = (col[2] + funcColors[f][2]) / 2.0;

			f = coeffs.length - 1;
			F(f, p, newp, totalp);
			col[0] = (col[0] + funcColors[f][0]) / 2.0;
			col[1] = (col[1] + funcColors[f][1]) / 2.0;
			col[2] = (col[2] + funcColors[f][2]) / 2.0;
		}

		for (int i = 0; i < numIter; i++) {
			int f = rand.nextInt(coeffs.length - 1);
			F(f, p, newp, totalp);
			col[0] = (col[0] + funcColors[f][0]) / 2.0;
			col[1] = (col[1] + funcColors[f][1]) / 2.0;
			col[2] = (col[2] + funcColors[f][2]) / 2.0;

			f = coeffs.length - 1;
			F(f, p, newp, totalp);
			col[0] = (col[0] + funcColors[f][0]) / 2.0;
			col[1] = (col[1] + funcColors[f][1]) / 2.0;
			col[2] = (col[2] + funcColors[f][2]) / 2.0;

			if (!(p[0] > 2 || p[0] < -2 || p[1] > 2 || p[1] < -2)) {
				int x = (int) (p[0] * width / 4 + width / 2);
				int y = (int) (p[1] * height / 4 + height / 2);
				data[x][y]++;
				colors[x][y][0] += col[0];
				colors[x][y][1] += col[1];
				colors[x][y][2] += col[2];
				colors[x][y][3]++;
			}
		}

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (data[i][j] != 0) {
					count++;
					double alpha = Math.log(colors[i][j][3]) / colors[i][j][3];
					float r = (float) (alpha * colors[i][j][0]);
					float gr = (float) (alpha * colors[i][j][1]);
					float b = (float) (alpha * colors[i][j][2]);
					r = Math.min(r, 1);
					gr = Math.min(gr, 1);
					b = Math.min(b, 1);
					g.setColor(new Color(r, gr, b));
					g.fillRect(i, j, 1, 1);
				}
			}
		}
		System.out.println("Painted " + count);
	}

	public void F(int f, double[] p, double[] newp, double[] totalp) {
		newp[0] = coeffs[f][0] * p[0] + coeffs[f][1] * p[1] + coeffs[f][2];
		newp[1] = coeffs[f][3] * p[0] + coeffs[f][4] * p[1] + coeffs[f][5];
		p[0] = newp[0];
		p[1] = newp[1];

		// Apply ALL the variations with their respective weights
		totalp[0] = 0;
		totalp[1] = 0;
		double r = Math.sqrt(p[0] * p[0] + p[1] * p[1]);
		double theta = Math.atan(p[0] / p[1]);
		double phi = Math.atan(p[1] / p[0]);
		for (int v = 0; v < variations.length; v++) {
			if (Double.compare(varWeights[v], 0) != 0) {
				variations[v].f(p, newp, coeffs[f], r, theta, phi, rand);
				totalp[0] += newp[0] * varWeights[v];
				totalp[1] += newp[1] * varWeights[v];
			}
		}
		p[0] = totalp[0];
		p[1] = totalp[1];

		// Post transform
		newp[0] = postCoeffs[f][0] * p[0] + postCoeffs[f][1] * p[1] + postCoeffs[f][2];
		newp[1] = postCoeffs[f][3] * p[0] + postCoeffs[f][4] * p[1] + postCoeffs[f][5];
		p[0] = newp[0];
		p[1] = newp[1];
	}

	public void initVariations() {
		variations = new Variation[49];
		varWeights = new double[49];
		variations[0] = new Variation.Linear();
		variations[1] = new Variation.Sinusoidal();
		variations[2] = new Variation.Spherical();
		variations[3] = new Variation.Swirl();
		variations[4] = new Variation.Horseshoe();
		variations[5] = new Variation.Polar();
		variations[6] = new Variation.Handkerchief();
		variations[7] = new Variation.Heart();
		variations[8] = new Variation.Disc();
		variations[9] = new Variation.Spiral();
		variations[10] = new Variation.Hyperbolic();
		variations[11] = new Variation.Diamond();
		variations[12] = new Variation.Ex();
		variations[13] = new Variation.Julia();
		variations[14] = new Variation.Bent();
		variations[15] = new Variation.Waves();
		variations[16] = new Variation.Fisheye();
		variations[17] = new Variation.Popcorn();
		variations[18] = new Variation.Exponential();
		variations[19] = new Variation.Power();
		variations[20] = new Variation.Cosine();
		variations[21] = new Variation.Rings();
		variations[22] = new Variation.Fan();
		variations[23] = new Variation.Blob(rand.nextDouble(), rand.nextDouble(), rand.nextDouble());
		variations[24] = new Variation.PDJ(rand.nextDouble(), rand.nextDouble(), rand.nextDouble(), rand.nextDouble());
		variations[25] = new Variation.Fan2();
		variations[26] = new Variation.Rings2();
		variations[27] = new Variation.Eyefish();
		variations[28] = new Variation.Bubble();
		variations[29] = new Variation.Cylinder();
		variations[30] = new Variation.Perspective();
		variations[31] = new Variation.Noise();
		variations[32] = new Variation.JuliaN();
		variations[33] = new Variation.JuliaScope();
		variations[34] = new Variation.Blur();
		variations[35] = new Variation.Gaussian();
		variations[36] = new Variation.RadialBlur();
		variations[37] = new Variation.Pie();
		variations[38] = new Variation.Ngon();
		variations[39] = new Variation.Curl();
		variations[40] = new Variation.Rectangles();
		variations[41] = new Variation.Arch();
		variations[42] = new Variation.Tangent();
		variations[43] = new Variation.Square();
		variations[44] = new Variation.Rays();
		variations[45] = new Variation.Blade();
		variations[46] = new Variation.Secant();
		variations[47] = new Variation.Twintrian();
		variations[48] = new Variation.Cross();

		// Normalise all the weights
		double total = 0;
		for (int i = 0; i < variations.length; i++) {
			if (Double.compare(rand.nextDouble(), 0.03) < 0) {
				varWeights[i] = rand.nextDouble() + 0.1;
				total += varWeights[i];
			} else {
				varWeights[i] = 0;
			}
		}
		for (int i = 0; i < variations.length; i++) {
			varWeights[i] /= total;
		}

	}

}
