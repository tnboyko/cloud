/*
 * File: Breakout.java
 * -------------------
 * Name:
 * Section Leader:
 * 
 * This file will eventually implement the game of Breakout.
 */

import java.awt.Color;
import java.awt.event.MouseEvent;

import acm.program.*;
import acm.graphics.*;

public class Breakout extends GraphicsProgram {

	public static final int BALL_DIAM = 25;

	public static final int PLAT_WIDTH = 100;
	public static final int PLAT_HEIGHT = 20;

	public static final int W_WIDTH = 800;
	public static final int W_HEIGHT = 600;

	public static final int DELAY = 7;
	public static int speed1 = 2;
	public static int speed2 = -2;
	public boolean lose = false;
	public boolean win = false;

	public static int count1 = 0;
	public static int count2 = 0;

	GOval ball;
	GRect plattform;
	GObject coll1;
	GObject coll2;
	GObject coll3;
	GObject coll4;

	public void run() {
		this.setSize(W_WIDTH, W_HEIGHT);
		ball = new GOval(W_WIDTH / 4, 3 * W_HEIGHT / 4, BALL_DIAM, BALL_DIAM);
		ball.setFilled(true);
		ball.setFillColor(Color.black);
		add(ball);
		plattform = new GRect(W_WIDTH / 2 - PLAT_WIDTH / 2, W_HEIGHT
				- PLAT_HEIGHT, PLAT_WIDTH, PLAT_HEIGHT);
		plattform.setFilled(true);
		plattform.setFillColor(Color.red);
		add(plattform);
		addMouseListeners();
		buildBricks();
		while (!lose && !win) {
			moveBall();
			checkCollision();
			checkWin();
			pause(DELAY);
		}
		if (lose == true) {
			GImage youLose = new GImage("S2e16_You_lose.png");
			add(youLose, 0, 0);
		} else {
			GImage youWin = new GImage("YouWin.png");
			add(youWin, 0, 0);
		}
	}

	private void checkWin() {
		if (count1 == count2)
			win = true;

	}

	private void buildBricks() {
		for (int i = 50; i < 300; i += 35) {
			for (int z = 7; z < 755; z += 72) {
				GRect rect = new GRect(z, i, 65, 25);
				rect.setFilled(true);
				if (i >= 50 && i < 120)
					rect.setFillColor(Color.yellow);
				if (i >= 120 && i < 190)
					rect.setFillColor(Color.blue);
				if (i >= 190 && i < 260)
					rect.setFillColor(Color.green);
				if (i >= 260)
					rect.setFillColor(Color.red);
				add(rect);
				count1++;
			}
		}

	}

	private void moveBall() {
		ball.move(speed1, speed2);

	}

	public void checkCollision() {
		coll1 = getElementAt(ball.getX(), ball.getY() + BALL_DIAM / 2);
		coll2 = getElementAt(ball.getX() + BALL_DIAM, ball.getY() + BALL_DIAM
				/ 2);
		coll3 = getElementAt(ball.getX() + BALL_DIAM / 2, ball.getY()
				+ BALL_DIAM);
		coll4 = getElementAt(ball.getX() + BALL_DIAM / 2, ball.getY());
		if (ball.getX() < 0) {
			speed1 = 2;
			return;
		} else if (ball.getX() + BALL_DIAM > getWidth()) {
			speed1 = -2;
			return;
		}
		if (ball.getY() > W_HEIGHT) {
			lose = true;
			remove(ball);
			return;
		}
		if (ball.getY() < 0) {
			speed2 = -speed2;
			return;
		}
		if (coll1 == plattform) {
			speed1 = -speed1;
		}
		if (coll2 == plattform) {
			speed1 = -speed1;
		}
		if (ball.getY() > W_HEIGHT - PLAT_HEIGHT) {
			if (ball.getX() + BALL_DIAM == plattform.getX()) {
				speed1 = -speed1;
			}
			if (ball.getX() == plattform.getX() + PLAT_WIDTH) {
				speed1 = -speed1;
			}
		}
		if (coll3 == plattform) {
			if (ball.getX() + BALL_DIAM >= plattform.getX()
					&& ball.getX() + BALL_DIAM <= plattform.getX() + 2
							* PLAT_WIDTH / 5) {
				speed2 = -speed2;
				if (speed1 > 0)
					speed1 = -1;
				else
					speed1 = 1;
				if (speed1 < 0)
					speed1 = 1;
				else
					speed1 = -1;
				return;
			}
			if (ball.getX() + BALL_DIAM >= plattform.getX() + 2 * PLAT_WIDTH
					/ 5
					&& ball.getX() + BALL_DIAM <= plattform.getX() + 3
							* PLAT_WIDTH / 5) {
				speed2 = -speed2;
				return;
			}
			if (ball.getX() + BALL_DIAM >= plattform.getX() + 3 * PLAT_WIDTH
					/ 5
					&& ball.getX() + BALL_DIAM <= plattform.getX() + PLAT_WIDTH) {
				speed2 = -speed2;
				if (speed1 > 0)
					speed1 = -1;
				else
					speed1 = 1;
				if (speed1 < 0)
					speed1 = 1;
				else
					speed1 = -1;
				return;
			}
		}
		if (coll1 != null && coll1 != plattform) {
			speed1 = -speed1;
			remove(coll1);
			count2++;
			return;
		} else if (coll2 != null && coll2 != plattform) {
			speed1 = -speed1;
			remove(coll2);
			count2++;
			return;
		} else if (coll3 != null && coll3 != plattform) {
			speed2 = -speed2;
			remove(coll3);
			count2++;
			return;
		} else if (coll4 != null && coll4 != plattform) {
			speed2 = -speed2;
			remove(coll4);
			count2++;
			return;
		}

	}

	public void mouseMoved(MouseEvent e) {
		int x = e.getX();
		plattform.setLocation(x, W_HEIGHT - PLAT_HEIGHT);
	}

}
