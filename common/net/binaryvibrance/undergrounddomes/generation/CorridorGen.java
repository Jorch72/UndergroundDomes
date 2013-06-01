package net.binaryvibrance.undergrounddomes.generation;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import net.binaryvibrance.undergrounddomes.generation.maths.Line;
import net.binaryvibrance.undergrounddomes.generation.maths.Point3D;
import net.binaryvibrance.undergrounddomes.generation.maths.Vector3;
import net.binaryvibrance.undergrounddomes.helpers.LogHelper;
import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class CorridorGen {
	private static final Logger LOG = LogHelper.getLogger();
	private final SphereChain sphereChain;

	List<Line> corridorPaths = new LinkedList<Line>();

	public CorridorGen(SphereChain sphereChain) {
		this.sphereChain = sphereChain;
	}

	public void generateCorridor() {
		int currentSphere = 1;
		int sphereCount = sphereChain.sphereChainLength;
		for (SphereInstance sphere : sphereChain.getChain()) {
			LOG.info(String.format("Creating corridors for sphere %d/%d", currentSphere++, sphereCount));
			// Calculate Nearest Neighbours
			SphereNearestNeighbour snn = new SphereNearestNeighbour(sphere);
			for (SphereInstance neighbourSphere : sphereChain.getChain()) {
				snn.addNeighbour(neighbourSphere);
			}

			// Build corridor between nearest triad

			SphereInstance primary = null;
			for (SphereInstance secondary : snn) {
				if (primary == null) {
					primary = secondary;
					continue;
				}
				
				boolean valid = true;
				List<Line> allPaths = new LinkedList<Line>();
				Point3D averagePoint = Point3D.average(sphere, primary, secondary);
				List<SphereInstance> spheres = new LinkedList<SphereInstance>(Arrays.asList(new SphereInstance[] {sphere, primary, secondary }));
				
				for (SphereInstance theSphere : spheres) {
					EntranceToCorridor currentPaths = generatePaths(theSphere, averagePoint);
					if (!currentPaths.isApplied()) {
						for (SphereInstance compareSphere : sphereChain.getChain()) {
							if (compareSphere.intersectsLine(currentPaths.a)) {
								LOG.info(String.format("Corridor %s intersects with sphere %s", currentPaths.a, compareSphere));
								valid = false;
								break;
							}
							if (compareSphere.intersectsLine(currentPaths.b)) {
								LOG.info(String.format("Corridor %s intersects with sphere %s", currentPaths.b, compareSphere));
								valid = false;
								break;
							}
						}
						if (!valid) {
							break;
						}
						
						allPaths.add(currentPaths.a);
						allPaths.add(currentPaths.b);
						currentPaths.markApplied();
					}
				}
				
				if (valid) {
					corridorPaths.addAll(allPaths);
					break;
				}
			}
		}
	}

	private EntranceToCorridor generatePaths(SphereInstance sphere, Point3D averagePoint) {
		SphereFloor baseFloor = sphere.getFloor(0);
		SphereEntrance northPoint = baseFloor.getEntrance(EnumFacing.NORTH);
		SphereEntrance southPoint = baseFloor.getEntrance(EnumFacing.SOUTH);
		SphereEntrance eastPoint = baseFloor.getEntrance(EnumFacing.EAST);
		SphereEntrance westPoint = baseFloor.getEntrance(EnumFacing.WEST);
		
		double northDistance = averagePoint.distance(northPoint.location);
		double southDistance = averagePoint.distance(southPoint.location);
		double eastDistance = averagePoint.distance(eastPoint.location);
		double westDistance = averagePoint.distance(westPoint.location);

		Line a = null;
		Line b = null;
		Point3D join;
		Vec3 adjustmentVector = null;
		if (northDistance <= southDistance && northDistance <= eastDistance && northDistance <= westDistance) {
			if (northPoint.corridorPath != null) {
				return northPoint.corridorPath;
			}
			join = new Point3D(northPoint.location.x, 0, averagePoint.z);
			a = new Line(northPoint.location, join);
			adjustmentVector = Vector3.SOUTH;
		} else if (southDistance <= northDistance && southDistance <= eastDistance && southDistance <= westDistance) {
			if (southPoint.corridorPath != null) {
				return southPoint.corridorPath;
			}
			join = new Point3D(southPoint.location.x, 0, averagePoint.z);
			a = new Line(southPoint.location, join);
			adjustmentVector = Vector3.NORTH;
		} else if (eastDistance <= northDistance && eastDistance <= southDistance && eastDistance <= westDistance) {
			if (eastPoint.corridorPath != null) {
				return eastPoint.corridorPath;
			}
			join = new Point3D(averagePoint.x, 0, eastPoint.location.z);
			a = new Line(eastPoint.location, join);
			adjustmentVector = Vector3.WEST;
		} else {
			if (westPoint.corridorPath != null) {
				return westPoint.corridorPath;
			}
			join = new Point3D(averagePoint.x, 0, westPoint.location.z);
			a = new Line(westPoint.location, join);
			adjustmentVector = Vector3.EAST;
		}
		b = new Line(join, averagePoint);

		return new EntranceToCorridor(a, b, adjustmentVector);
	}

	public void renderCorridor(World world) {
		int offsetX = sphereChain.startX;
		int offsetZ = sphereChain.startZ;
		int offsetY = sphereChain.heightOffset;

		int currentLine = 1;
		int maxLines = corridorPaths.size();
		for (Line path : corridorPaths) {
			LOG.info(String.format("Creating Line %d/%d %s", currentLine++, maxLines, path.toString()));
			Vec3 vector = path.getRenderVector();
			Point3D currentPoint = path.start;
			do {
				world.setBlock(currentPoint.xCoord + offsetX, currentPoint.yCoord + offsetY, currentPoint.zCoord + offsetZ,
						Block.blockGold.blockID, 0, 0);
				currentPoint = currentPoint.add(vector);
			} while (!currentPoint.equals(path.end));
			world.setBlock(currentPoint.xCoord + offsetX, currentPoint.yCoord + offsetY, currentPoint.zCoord + offsetZ,
					Block.blockGold.blockID, 0, 0);
		}
	}
}
