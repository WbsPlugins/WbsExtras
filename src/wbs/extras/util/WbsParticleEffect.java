package wbs.extras.util;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.Particle.DustOptions;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class WbsParticleEffect {
	private static Vector upVector = new Vector(0, 1, 0);

	private static Plugin pl;
	public static void setPlugin(Plugin plugin) {
		pl = plugin;
	}
	
	
	public enum EffectType {
		SPIRAL, RING, ELECTRIC, LINE
	}
	
	private EffectType type;
	
	private double speed = 0;
	private double radius = 1;
	private int amount = 1;
	private int ticks = 5;
	private double rotation = 0;
	
	private boolean clockwise = true; // For spiral
	
	private Vector about = upVector; // The vector about which circles are drawn
	private Vector direction = upVector; // The vector that determines direction for speeds
	private double variation = 0; // The angle in degrees that direction will be randomized to
	
	private Object options = null;
	
	public WbsParticleEffect(EffectType type) {
		this.type = type;
	}


	/*****************************/
	/*        RUN METHODS        */
	/*****************************/
	
	public void run(Particle particle, Location loc) {
		switch (type) {
		case ELECTRIC:
			electric(particle, loc);
			break;
		case LINE:
			pl.getLogger().warning("Line particle effect requires two locations - Using random location");
			line(particle, loc, loc.clone().add(randomVector(radius)));
			break;
		case RING:
			ring(particle, loc);
			break;
		case SPIRAL:
			spiral(particle, loc);
			break;
		default:
			break;
		}
	}

	public void run(Particle particle, Location start, Location finish) {
		switch (type) {
		case ELECTRIC:
			electric(particle, start);
			break;
		case LINE:
			line(particle, start, finish);
			break;
		case RING:
			ring(particle, start);
			break;
		case SPIRAL:
			spiral(particle, start);
			break;
		default:
			break;
		}
	}
	
	/******************************/
	/*        TYPE METHODS        */
	/******************************/
	
	private void spiral(Particle particle, Location loc) {
		World world = loc.getWorld();
		ArrayList<Location> startPoints;
		Vector direction = about;
		if (about.equals(upVector)) {
			startPoints = getPoints(loc, amount, radius, rotation);
		} else {
			startPoints = getPointsXYZ(loc, amount, radius, about, rotation);
		}
		direction.normalize().multiply(1);


		new BukkitRunnable() {
			int escape = 0;
			int i;
			Location velPoint;
			@Override
            public void run() {
				if (escape < ticks) {
					i = 0;
					for (Location point : startPoints) {
						i++;
						if (clockwise) {
							velPoint = startPoints.get((i + (startPoints.size() / 4)) % startPoints.size());
						} else {
							int pointerIndex = (i - (startPoints.size() / 4)) % startPoints.size();
							while (pointerIndex < 0) {
								pointerIndex += startPoints.size();
							}
							
							velPoint = startPoints.get(pointerIndex);
						}
						Vector vec = velPoint.clone().subtract(loc.toVector()).toVector();
						vec = scaleVector(vec, variation);
						Vector vecSave = vec;
						for (int k = 0; k < amount; k++) {
							vec = vecSave.clone();
							if (options == null) {
								world.spawnParticle(particle, point, 0, vec.getX() + direction.getX(), vec.getY() + direction.getY(), vec.getZ() + direction.getZ(), speed);
							} else {
								world.spawnParticle(particle, point, 0, vec.getX() + direction.getX(), vec.getY() + direction.getY(), vec.getZ() + direction.getZ(), speed, particle.getDataType().cast(options));
							}
						}
					}
				} else {
					cancel();
				}
				escape++;
			}
        }.runTaskTimer(pl, 0L, 1L);
	}
	
	private void ring(Particle particle, Location loc) {
		World world = loc.getWorld();

		ArrayList<Location> points = getPointsXYZ(loc, amount, radius, about, rotation);
		
		if (about.equals(upVector)) {
			points = getPoints(loc, amount, radius, rotation);
		} else {
			points = getPointsXYZ(loc, amount, radius, about, rotation);
		}
		
		if (options == null) {
			for (Location point : points) {
				world.spawnParticle(particle, point, 0, direction.getX() + rand(variation), direction.getY() + rand(variation), direction.getZ() + rand(variation), speed, null, true);
			}
		} else {
			for (Location point : points) {
				world.spawnParticle(particle, point, 0, direction.getX(), direction.getY(), direction.getZ(), speed, particle.getDataType().cast(options), true);
			}
		}
	}
	
	private void electric(Particle particle, Location loc) {
		new BukkitRunnable() {
			Location newPoint;
			int escape = 0;
			@Override
			public void run() {
				escape++;
				if (escape > ticks) {
					cancel();
				}
				for (int i = 0; i < amount; i++) {
					newPoint = loc.clone().add(randomVector(radius*2));
					line(particle, newPoint, newPoint.clone().add(randomVector(Math.random()*0.25+0.5)));
				}
			}
        }.runTaskTimer(pl, 0L, 2L);
	}
	
	private void line(Particle particle, Location start, Location finish) {
		World world = start.getWorld();
		Vector startToFinish = (finish.clone().subtract(start)).toVector();
		Vector direction = scaleVector(startToFinish, start.distance(finish) / amount);
		Location point = start.clone();
		if (options == null) {
			for (int i = 0; i < amount; i++) {
				world.spawnParticle(particle, point, 1, radius, radius, radius, speed);
				point = point.add(direction);
			}
		} else {
			for (int i = 0; i < amount; i++) {
				world.spawnParticle(particle, point, 1, radius, radius, radius, speed, particle.getDataType().cast(options));
				point = point.add(direction);
			}
		}
	}
	
	/******************************/
	/*       UTILITY METHODS      */
	/******************************/
	
	
	/******************************/
	/*        MATH METHODS        */
	/******************************/
	
	private static Vector scaleVector(Vector original, double magnitude) {
		return (original.clone().normalize().multiply(magnitude));
	}
	
	private static double rand(double max) {
		double returnVal = Math.random();
		returnVal = returnVal * max;
		return returnVal;
	}
	
	private static Vector randomVector(double magnitude) {
		double x, y, z;
		x = Math.random()*2-1;
		y = Math.random()*2-1;
		z = Math.random()*2-1;
		
		double scale = Math.sqrt((x*x) + (y*y) + (z*z)) / magnitude;
		
		x/=scale;
		y/=scale;
		z/=scale;
		
		return new Vector(x, y, z);
	}
	
	private ArrayList<Location> getPoints(Location loc, int n, double radius, double rotation) {
		ArrayList<Location> points = new ArrayList<>();
		Location addLoc;
		double x, z, theta = rotation;
		double angle = 2* Math.PI / n;
		for (int i = 0; i < n; i++) {
			addLoc = loc.clone();
			
			x = (radius * Math.cos(theta));
			z = (radius * Math.sin(theta));
			
			addLoc.add(x, 0, z);
			points.add(addLoc);
			
			theta += angle;
		}
		return points;
	}
	
	private ArrayList<Location> getPointsXYZ(Location loc, int n, double radius, Vector vec, double rotation) {
		ArrayList<Location> points = new ArrayList<>();
		Location addLoc;
		double x, y, z, theta = rotation;
		double angle = (2* Math.PI / n);
		
		double cx,cz;
		cx = vec.getX();
		cz = vec.getZ();
		
		Vector v = vec.clone().normalize();

		double ax,ay,az;
		ax = cz;
		ay = 0;
		az = -cx;
		
		Vector a = new Vector(ax, ay, az);
		a.normalize();
		Vector b = a.clone().crossProduct(v);
		b.normalize();
		
		for (int i = 0; i < n; i++) {
			double cosAngle = Math.cos(theta);
			double sinAngle = Math.sin(theta);
			addLoc = loc.clone();
			
			x = (radius*cosAngle*a.getX()) + (radius*sinAngle*b.getX());
			y = (radius*cosAngle*a.getY()) + (radius*sinAngle*b.getY());
			z = (radius*cosAngle*a.getZ()) + (radius*sinAngle*b.getZ());
			
			addLoc.add(x, y, z);
			points.add(addLoc);
			
			theta = (theta + angle) % (2*Math.PI);
			
		}
		return points;
	}

	/*****************/
	/*     CLONE     */
	/*****************/
	

	public WbsParticleEffect clone() {
		WbsParticleEffect clone = new WbsParticleEffect(type);
		
		clone.setAbout(about);
		clone.setAmount(amount);
		clone.setClockwise(clockwise);
		clone.setOptions(options);
		clone.setRadius(radius);
		clone.setSpeed(speed);
		clone.setTicks(ticks);
		
		return clone;
	}

	/*********************************/
	/*        GETTERS/SETTERS        */
	/*********************************/
	

	/**
	 * @return the radius
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * @param radius the radius to set
	 */
	public void setRadius(double radius) {
		this.radius = radius;
	}

	
	
	/**
	 * @return the speed
	 */
	public double getSpeed() {
		return speed;
	}

	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}

	
	
	/**
	 * @return the amount
	 */
	public int getAmount() {
		return amount;
	}

	/**
	 * @param amount the amount to set
	 */
	public void setAmount(int amount) {
		this.amount = amount;
	}

	
	
	/**
	 * @return the ticks
	 */
	public int getTicks() {
		return ticks;
	}

	/**
	 * @param ticks the ticks to set
	 */
	public void setTicks(int ticks) {
		this.ticks = ticks;
	}

	

	/**
	 * @return the vector about which circles are drawn
	 */
	public Vector getAbout() {
		return about;
	}

	/**
	 * @param about the vector about which circles are drawn
	 */
	public void setAbout(Vector about) {
		this.about = about;
	}
	

	/**
	 * @return the vector about which circles are drawn
	 */
	public Vector getDirection() {
		return direction;
	}

	/**
	 * @param about the vector about which circles are drawn
	 */
	public void setDirection(Vector direction) {
		this.about = direction;
	}
	
	/**
	 * @param options the options to set
	 */
	public void setOptions(Object options) {
		this.options = options;
	}
	
	/**
	 * @return the clockwise
	 */
	public boolean isClockwise() {
		return clockwise;
	}
	
	/**
	 * @param clockwise the clockwise to set
	 */
	public void setClockwise(boolean clockwise) {
		this.clockwise = clockwise;
	}


	public double getRotation() {
		return rotation;
	}


	public void setRotation(double rotation) {
		this.rotation = rotation;
	}


	public double getVariation() {
		return variation;
	}


	public void setVariation(double variation) {
		this.variation = variation;
	}
	

	
}
