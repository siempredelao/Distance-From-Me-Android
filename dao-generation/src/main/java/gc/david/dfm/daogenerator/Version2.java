package gc.david.dfm.daogenerator;

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

/**
 * Created by David on 29/10/2014.
 * Just giving a proper name to entities.
 */
public class Version2 extends Version1 {

	private static final String DISTANCE_NAME = "Distance";
	private static final String POSITION_NAME = "Position";

	public Version2(boolean current) {
		super(current);

		final Schema schema = getSchema();

		final Entity distanceEntity = addDistanceEntity(schema);
		final Entity positionEntity = addPositionEntity(schema);

		final Property distanceIdProperty = positionEntity.addLongProperty("distanceId").notNull().getProperty();
		distanceEntity.addToMany(positionEntity, distanceIdProperty);

		removeEntryEntity(schema);
	}

	private Entity addDistanceEntity(final Schema schema) {
		final Entity distance = schema.addEntity(DISTANCE_NAME);

		distance.addIdProperty().autoincrement();
		distance.addStringProperty("name").notNull();
		distance.addStringProperty("distance").notNull();
		distance.addDateProperty("date").notNull();

		return distance;
	}

	private Entity addPositionEntity(final Schema schema) {
		final Entity position = schema.addEntity(POSITION_NAME);

		position.addIdProperty().autoincrement();
		position.addDoubleProperty("latitude").notNull();
		position.addDoubleProperty("longitude").notNull();

		return position;
	}

	private void removeEntryEntity(final Schema schema) {
		removeEntity("Entry", schema);
	}

	@Override
	public int getVersionNumber() {
		return 2;
	}
}
