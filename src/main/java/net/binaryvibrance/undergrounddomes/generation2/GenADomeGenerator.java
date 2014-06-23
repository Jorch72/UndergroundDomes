package net.binaryvibrance.undergrounddomes.generation2;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import net.binaryvibrance.helpers.maths.Point3D;
import net.binaryvibrance.undergrounddomes.generation2.contracts.IDomeGenerator;
import net.binaryvibrance.undergrounddomes.generation2.model.Dome;
import net.binaryvibrance.undergrounddomes.generation2.model.DomeFloor;
import net.binaryvibrance.undergrounddomes.helpers.LogHelper;


public class GenADomeGenerator implements IDomeGenerator {
	
	private static final Logger LOG = LogHelper.getLogger();
    private static final int MIN_FLOOR_SIZE = 5;
	private Random random;

	@Override
	public List<Dome> Generate() {
		List<Dome> domes;
		domes = createDomeChain();
		//Process Dome purpose
		return domes;
	}
	
	@Override
	public void SetRandom(Random random) {
		this.random = random;
	}
	
	private List<Dome> createDomeChain() {

		//Create up to 16 domes.
		int domeChainLength = random.nextInt(12) + 4;
		LinkedList<Dome> chain = new LinkedList<Dome>();
		int buildLength = 0;

		Dome previousDome = null;
		while (buildLength < domeChainLength) {
			Dome dome = null;
			int domeScore = Integer.MIN_VALUE;
			for (int i = 0; i < 10; i++) {
				Dome potentialDome = getPotentialDome(previousDome);
				if (!isValid(potentialDome, chain)) {
					continue;
				}
				int potentialDomeScore = getDomeScore(potentialDome, chain);
				if (potentialDomeScore > domeScore) {
					dome = potentialDome;
					domeScore = potentialDomeScore;
				}
			}
			if (dome == null) {
				break;
			}
			createFloors(dome);
            Point3D location = dome.getLocation();
			LOG.info(String.format("Dome %d/%d @ (%d,%d,%d) d:%d", buildLength + 1, domeChainLength, location.xCoord, location.yCoord,
                    location.zCoord, dome.getDiameter()));
			chain.add(dome);

			//heightOffset = Math.max(heightOffset, (int) dome.getRadius() + 1);

			previousDome = dome;
			buildLength++;
		}

		//domeChainLength = chain.size();
        return chain;
	}
	
	private int getDomeScore(Dome potentialDome, List<Dome> chain) {
        Point3D potentialDomeLocation = potentialDome.getLocation();
		double x = potentialDomeLocation.x;
		double y = potentialDomeLocation.y;
		double z = potentialDomeLocation.z;
		int count = 1;

		for (Dome dome : chain) {
            Point3D domeLocation = dome.getLocation();
			x += domeLocation.x;
			y += domeLocation.y;
			z += domeLocation.z;
			count++;
		}

		double offsetX = x / count;
		double offsetY = y / count;
		double offsetZ = z / count;

		double distance = -Math.sqrt(Math.pow(offsetX, 2) + Math.pow(offsetY, 2) + Math.pow(offsetZ, 2));

		return (int) distance;
	}

	private boolean isValid(Dome dome, List<Dome> chain) {
		for (Dome existingDome : chain) {
			double checkDistance = dome.getLocation().distance(existingDome.getLocation());
			/*
			 * double checkDistance = Math.pow(dome.x - existingDome.x, 2) +
			 * Math.pow(dome.y - existingDome.y, 2) + Math.pow(dome.z -
			 * existingDome.z, 2);
			 */
			double minimumDistance = Math.pow(dome.getRadius() + existingDome.getRadius(), 2);
			if (checkDistance < minimumDistance)
				return false;
		}

		return true;
	}

	private Dome getPotentialDome(Dome previousDome) {
		final int minCorridorSpacing = 6;
		int originX;
		int originZ;

		int diameter = random.nextInt(16) + 10;
		diameter = diameter + diameter % 2;
		final double radius = diameter / 2.0f;

		final int xDirection = random.nextBoolean() ? -1 : 1;
		final int zDirection = random.nextBoolean() ? -1 : 1;
		final boolean firstDirectionIsXAxis = random.nextBoolean();
		final double firstDimensionOffset = radius + random.nextInt(12);
		final int newSpacing = minCorridorSpacing + random.nextInt(8);

		final int previousDomeDiameter = previousDome != null ? previousDome.getDiameter() : 0;
		final int previousDomeX = previousDome != null ? previousDome.getLocation().xCoord : 0;
		final int previousDomeZ = previousDome != null ? previousDome.getLocation().zCoord : 0;

		final double touchingDistance = previousDomeDiameter / 2.0f + radius;
		// FIXME: Do I even need to do this? Corridors aren't created here anymore.
		if (firstDirectionIsXAxis) {
			originZ = (int) (previousDomeZ + (firstDimensionOffset + minCorridorSpacing) * zDirection);
			originX = (int) (previousDomeX + (minCorridorSpacing
					+ Math.sqrt(Math.pow(Math.max(touchingDistance, firstDimensionOffset + 1), 2) - Math.pow(firstDimensionOffset, 2)) + newSpacing)
					* xDirection);
			LOG.finer(String.format("originX: %d, prevX: %d, touch: %f, offset: %f, spacing: %d", originX, previousDomeX,
					touchingDistance, firstDimensionOffset, newSpacing));
		} else {
			originX = (int) (previousDomeX + (firstDimensionOffset + minCorridorSpacing) * xDirection);
			originZ = (int) (previousDomeZ + (minCorridorSpacing
					+ Math.sqrt(Math.pow(Math.max(touchingDistance, firstDimensionOffset + 1), 2) - Math.pow(firstDimensionOffset, 2)) + newSpacing)
					* zDirection);
			LOG.finer(String.format("originZ: %d, prevZ: %d, touch: %f, offset: %f, spacing: %d", originZ, previousDomeZ,
					touchingDistance, firstDimensionOffset, newSpacing));
		}

		LOG.info(String.format("Dome @ (%d,%d,%d) d:%d", originX, 0, originZ, diameter));
		Dome dome = new Dome(new Point3D(originX, 0, originZ), diameter);
		return dome;
	}

    public void createFloors(Dome dome) {
        int available = (int) ((dome.getDiameter() - 2) * 0.75); // Don't include walls
        int maxFloors = (int) Math.floor(available / (float) MIN_FLOOR_SIZE);
        LOG.info("MaxFloors: " + maxFloors);
        int actualFloors = maxFloors;// == 1 ? 1 : random.nextInt(maxFloors - 1)
        // + 1;
        int interval = (int) Math.ceil(available / actualFloors);
        int variance = interval - MIN_FLOOR_SIZE;

        int baseHeight = dome.getDiameter() - available - 2;
        dome.addFloor(new DomeFloor(dome, baseHeight));
        LOG.info(String.format("Floor 0 at level %d", baseHeight));
        for (int floor = 1; floor < actualFloors; ++floor) {
            int floorVariance = random.nextBoolean() ? 1 : -1;
            int floorStart = baseHeight + floor * interval + variance * floorVariance;
            LOG.info(String.format("Floor %d at level %d", floor, floorStart));
            dome.addFloor(new DomeFloor(dome, floorStart));
        }
    }


}