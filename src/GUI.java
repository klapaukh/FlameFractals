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


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GUI extends JComponent {

	private static final long serialVersionUID = -8900010760721989010L;
	double[][] coeffs;// = new double[][] { { 0.5, 0, 0, 0, 0.5, 0 }, { 0.5, 0, 0.5, 0, 0.5, 0 }, { 0.5, 0, 0, 0, 0.5,
						// 0.5 }, { 1, 0, 0, 0, 1, 0 } };
	double[][] postCoeffs;// = new double[][] { { 1, 0, 0, 0, 1, 0 }, { 1, 0, 0, 0, 1, 0 }, { 1, 0, 0, 0, 1, 0 }, { 1,
							// 0, 0, 0, 1, 0 } };
	double[][] funcColors;// = new double[][] { { 0, 1, 0 }, { 1, 0, 0 }, { 0, 0, 1 }, { 0.5, 0.5, 0.5 } };
	long numIter = 100000;
	Random rand = new Random();
	Variation[] variations;
	double[] varWeights;
	int count;
	boolean run;
	boolean done;
	int zoom = 1;
	boolean useOld = false;
	double gamma = 4;
	int[][] data;
	double[][][] colors;
	int superSampleSize = 3;

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
		// for (int i = 0; i < coeffs.length; i++) {
		// System.out.printf("%.2fx + %.2fy + %.2f\n", coeffs[i][0], coeffs[i][1], coeffs[i][2]);
		// System.out.printf("%.2fx + %.2fy + %.2f\n\n", coeffs[i][3], coeffs[i][4], coeffs[i][5]);
		// }
		initVariations();
		JFrame frame = new JFrame("Chaos Game " + seed);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(1024, 768);
		frame.getContentPane().add(this, BorderLayout.CENTER);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		// Iterations
		JPanel panel = new JPanel();
		JSlider slider = new JSlider(4, 10, 5);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				numIter = (long) Math.pow(10, ((JSlider) e.getSource()).getValue());
				useOld = false;
				repaint();
			}
		});
		panel.setLayout(new BorderLayout());
		panel.add(slider, BorderLayout.CENTER);
		panel.add(new JLabel("Iterations"), BorderLayout.WEST);
		mainPanel.add(panel);

		// Zoom
		panel = new JPanel();
		slider = new JSlider(1, 10, 1);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				zoom = ((JSlider) e.getSource()).getValue();
				useOld = false;
				repaint();
			}
		});
		panel.setLayout(new BorderLayout());
		panel.add(slider, BorderLayout.CENTER);
		panel.add(new JLabel("Zoom"), BorderLayout.WEST);
		mainPanel.add(panel);

		// Gamma
		panel = new JPanel();
		slider = new JSlider(1, 100, (int) (gamma * 10));
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				gamma = ((JSlider) e.getSource()).getValue() / 10.0;
				useOld = true;
				repaint();
			}
		});
		panel.setLayout(new BorderLayout());
		panel.add(slider, BorderLayout.CENTER);
		panel.add(new JLabel("Gamma"), BorderLayout.WEST);
		mainPanel.add(panel);

		panel = new JPanel();
		JButton b= new JButton("Redraw");
		b.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});
		panel.add(b);
		mainPanel.add(b);

		JPanel topPanel = new JPanel();

		for(int i = 0; i< variations.length;i++){
			JLabel lab = new JLabel(variations[i].toString());
			final JTextField t = new JTextField(4);
			final int l = i;

			t.addKeyListener(new KeyListener(){
				int slot = l;
				JTextField f= t;
				@Override
				public void keyTyped(KeyEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void keyPressed(KeyEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void keyReleased(KeyEvent e) {
					try{
					varWeights[slot] = Double.parseDouble(f.getText());
					useOld = false;
					}catch (Exception exp){
						exp.printStackTrace();
					}
				}});

			t.setText(String.format("%.3f",varWeights[i]));
			topPanel.add(lab);
			topPanel.add(t);
		}

		JScrollPane scroll = new JScrollPane(topPanel);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		scroll.setPreferredSize(new Dimension(1000,50));

		frame.getContentPane().add(scroll,BorderLayout.NORTH);
		frame.getContentPane().add(mainPanel, BorderLayout.SOUTH);
		frame.setVisible(true);

		while (true) {
			while (!done) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
			if (count < 10) {
				seed = rand.nextLong();
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
				// for (int i = 0; i < coeffs.length; i++) {
				// System.out.printf("%.2fx + %.2fy + %.2f\n", coeffs[i][0], coeffs[i][1], coeffs[i][2]);
				// System.out.printf("%.2fx + %.2fy + %.2f\n\n", coeffs[i][3], coeffs[i][4], coeffs[i][5]);
				// }
				initVariations();
				done = false;
				useOld = false;
				this.repaint();

			} else {
				return;
			}
		}

	}

	public static void main(String args[]) {
		new GUI();
	}

	public void paint(Graphics g) {
		done = false;
		count = 0;
		int width = this.getWidth();
		int height = this.getHeight();

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);

		int h = height * superSampleSize;
		int w = width * superSampleSize;
		if (!useOld || data.length != w || data[0].length != h) {
			if (data == null || data.length != w || data[0].length != h) {
				data = new int[w][h];
				colors = new double[w][h][4];
			} else {
				for (int i = 0; i < w; i++) {
					for (int j = 0; j < h; j++) {
						data[i][j] = 0;
						for (int k = 0; k < 4; k++) {
							colors[i][j][k] = 0;
						}
					}
				}
			}

			double[] p = new double[] { rand.nextDouble(), rand.nextDouble() }, newp = new double[2], totalp = new double[2], col = new double[] {
					rand.nextDouble(), rand.nextDouble(), rand.nextDouble() };
			for (int i = 0; i < 20 && useOld; i++) {
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

			for (int i = 0; i < numIter && !useOld; i++) {
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

				if (!(p[0] > zoom || p[0] < -zoom || p[1] > zoom || p[1] < -zoom)) {
					int x = (int) (p[0] * w / (zoom * 2) + w / 2);
					int y = (int) (p[1] * h / (zoom * 2) + h / 2);
					data[x][y]++;
					colors[x][y][0] += col[0];
					colors[x][y][1] += col[1];
					colors[x][y][2] += col[2];
					colors[x][y][3]++;
				}
			}
			useOld = true;
		}

		int ss = superSampleSize / 2, c=0;;
		boolean draw;
		for (int i = superSampleSize / 2; i < w; i += superSampleSize) {
			for (int j = superSampleSize / 2; j < h; j += superSampleSize) {
				float rt = 0, gt = 0, bt = 0;
				draw = false;
				c=0;
				for (int x = i - ss; x <= i + ss; x++) {
					for (int y = j - ss; y <= j + ss; y++) {
						if (data[x][y] != 0) {
							draw = true;
							double alpha = Math.log(colors[x][y][3]) / colors[x][y][3];
							float r = (float) (alpha * colors[x][y][0]);
							float gr = (float) (alpha * colors[x][y][1]);
							float b = (float) (alpha * colors[x][y][2]);
							r = (float) Math.pow(r, gamma);
							gr = (float) Math.pow(gr, gamma);
							b = (float) Math.pow(b, gamma);
							r = Math.min(r, 1);
							gr = Math.min(gr, 1);
							b = Math.min(b, 1);
							rt += r;
							gt += gr;
							bt += b;
							c++;
						}
					}
				}
				rt/=c;
				gt/=c;
				bt/=c;
				if (draw) {
					count++;
					g.setColor(new Color(rt, gt, bt));
					g.fillRect(i/superSampleSize, j/superSampleSize, 1, 1);
				}
			}
		}
		System.out.println("Painted " + count + " at zoom " + zoom);
		done = true;
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

		boolean var = false;
		for (int v = 0; v < variations.length; v++) {
			if (Double.compare(varWeights[v], 0) != 0) {
				var = true;
				variations[v].f(p, newp, coeffs[f], r, theta, phi, rand);
				totalp[0] += newp[0] * varWeights[v];
				totalp[1] += newp[1] * varWeights[v];
			}
		}
		if (var) {
			p[0] = totalp[0];
			p[1] = totalp[1];
		}

		// Post transform
		newp[0] = postCoeffs[f][0] * p[0] + postCoeffs[f][1] * p[1] + postCoeffs[f][2];
		newp[1] = postCoeffs[f][3] * p[0] + postCoeffs[f][4] * p[1] + postCoeffs[f][5];
		p[0] = newp[0];
		p[1] = newp[1];
	}

	public void initVariations() {
		variations = new Variation[49];
		varWeights = new double[49];

		// Normalise all the weights
		double total = 0;
		for (int i = 0; i < variations.length; i++) {
			if (Double.compare(rand.nextDouble(), 0.3) < 0) {
				varWeights[i] = rand.nextDouble() + 0.1;
				total += varWeights[i];
			} else {
				varWeights[i] = 0;
			}
		}
		for (int i = 0; i < variations.length; i++) {
			varWeights[i] /= total;
		}

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
		variations[25] = new Variation.Fan2(rand.nextDouble(), rand.nextDouble());
		variations[26] = new Variation.Rings2(rand.nextDouble());
		variations[27] = new Variation.Eyefish();
		variations[28] = new Variation.Bubble();
		variations[29] = new Variation.Cylinder();
		variations[30] = new Variation.Perspective(rand.nextDouble() * Math.PI * 2, rand.nextGaussian());
		variations[31] = new Variation.Noise();
		variations[32] = new Variation.JuliaN(rand.nextGaussian(), rand.nextGaussian());
		variations[33] = new Variation.JuliaScope(rand.nextGaussian(), rand.nextGaussian());
		variations[34] = new Variation.Blur();
		variations[35] = new Variation.Gaussian();
		variations[36] = new Variation.RadialBlur(rand.nextDouble() * Math.PI * 2, varWeights[36]);
		variations[37] = new Variation.Pie(rand.nextInt(10), rand.nextDouble() * Math.PI * 2, rand.nextDouble());
		variations[38] = new Variation.Ngon(rand.nextDouble() * 5, rand.nextInt(10), rand.nextInt(12), rand.nextGaussian());
		variations[39] = new Variation.Curl(rand.nextGaussian(), rand.nextGaussian());
		variations[40] = new Variation.Rectangles(rand.nextGaussian(), rand.nextGaussian());
		variations[41] = new Variation.Arch(varWeights[41]);
		variations[42] = new Variation.Tangent();
		variations[43] = new Variation.Square();
		variations[44] = new Variation.Rays(varWeights[44]);
		variations[45] = new Variation.Blade(varWeights[45]);
		variations[46] = new Variation.Secant(varWeights[46]);
		variations[47] = new Variation.Twintrian(varWeights[47]);
		variations[48] = new Variation.Cross();

	}

}
