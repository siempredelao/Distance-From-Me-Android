package gc.david.dfm.migration;

import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Migration from Version1 to Version2
 * <p/>
 * Created by David on 29/10/2014.
 */
public class MigrateV1ToV2 extends MigrationImpl {

	@Override
	public int applyMigration(final SQLiteDatabase db, final int currentVersion) {
		prepareMigration(db, currentVersion);

		final gc.david.dfm.model.v1.DaoMaster previousDaoMaster = new gc.david.dfm.model.v1.DaoMaster(db);
		final gc.david.dfm.model.v1.DaoSession previousDaoSession = previousDaoMaster.newSession();
		final gc.david.dfm.model.v1.EntryDao previousEntryDao = previousDaoSession.getEntryDao();

		final gc.david.dfm.model.v2.DaoMaster newDaoMaster = new gc.david.dfm.model.v2.DaoMaster(db);
		final gc.david.dfm.model.v2.DaoSession newDaoSession = newDaoMaster.newSession();
		final gc.david.dfm.model.v2.DistanceDao newDistanceDao = newDaoSession.getDistanceDao();
		final gc.david.dfm.model.v2.PositionDao newPositionDao = newDaoSession.getPositionDao();

		final List<gc.david.dfm.model.v1.Entry> previousEntrys = previousEntryDao.loadAll();

		// WTF?!
		gc.david.dfm.model.v2.DistanceDao.createTable(db, true);
		gc.david.dfm.model.v2.PositionDao.createTable(db, true);

		for (final gc.david.dfm.model.v1.Entry previousEntry : previousEntrys) {
			final gc.david.dfm.model.v2.Distance newDistance = new gc.david.dfm.model.v2.Distance();
			newDistance.setName(previousEntry.getNombre());
			newDistance.setDistance(previousEntry.getDistancia());
			newDistance.setDate(fromStringtoDateObject(previousEntry.getFecha()));
			final long distanceId = newDistanceDao.insert(newDistance);

			final gc.david.dfm.model.v2.Position newPositionOrigin = new gc.david.dfm.model.v2.Position();
			newPositionOrigin.setLatitude(previousEntry.getLat_origen());
			newPositionOrigin.setLongitude(previousEntry.getLon_origen());
			newPositionOrigin.setDistanceId(distanceId);
			newPositionDao.insert(newPositionOrigin);

			final gc.david.dfm.model.v2.Position newPositionDestination = new gc.david.dfm.model.v2.Position();
			newPositionDestination.setLatitude(previousEntry.getLat_destino());
			newPositionDestination.setLongitude(previousEntry.getLon_destino());
			newPositionDestination.setDistanceId(distanceId);
			newPositionDao.insert(newPositionDestination);
		}

		return getMigratedVersion();
	}

	/**
	 * Converts a string containing a date to a Date object.
	 *
	 * @param stringDate string containing the date in "yyyy-MM-dd" format
	 * @return A Date object with year = yyyy, month = MM, day = dd
	 */
	private Date fromStringtoDateObject(final String stringDate) {
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return simpleDateFormat.parse(stringDate);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int getSourceVersion() {
		return 1;
	}

	@Override
	public int getMigratedVersion() {
		return 2;
	}

	@Override
	public Migration getPreviousMigration() {
		return null;
	}
}
