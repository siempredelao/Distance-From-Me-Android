package gc.david.dfm.model;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import gc.david.dfm.model.Distance;
import gc.david.dfm.model.Position;

import gc.david.dfm.model.DistanceDao;
import gc.david.dfm.model.PositionDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig distanceDaoConfig;
    private final DaoConfig positionDaoConfig;

    private final DistanceDao distanceDao;
    private final PositionDao positionDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        distanceDaoConfig = daoConfigMap.get(DistanceDao.class).clone();
        distanceDaoConfig.initIdentityScope(type);

        positionDaoConfig = daoConfigMap.get(PositionDao.class).clone();
        positionDaoConfig.initIdentityScope(type);

        distanceDao = new DistanceDao(distanceDaoConfig, this);
        positionDao = new PositionDao(positionDaoConfig, this);

        registerDao(Distance.class, distanceDao);
        registerDao(Position.class, positionDao);
    }
    
    public void clear() {
        distanceDaoConfig.clearIdentityScope();
        positionDaoConfig.clearIdentityScope();
    }

    public DistanceDao getDistanceDao() {
        return distanceDao;
    }

    public PositionDao getPositionDao() {
        return positionDao;
    }

}
