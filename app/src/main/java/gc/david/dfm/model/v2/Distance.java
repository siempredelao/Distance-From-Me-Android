package gc.david.dfm.model.v2;

import java.util.List;
import gc.david.dfm.model.v2.DaoSession;
import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table DISTANCE.
 */
public class Distance {

    private Long id;
    /** Not-null value. */
    private String name;
    /** Not-null value. */
    private String distance;
    /** Not-null value. */
    private java.util.Date date;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient DistanceDao myDao;

    private List<Position> positionList;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public Distance() {
    }

    public Distance(Long id) {
        this.id = id;
    }

    public Distance(Long id, String name, String distance, java.util.Date date) {
        this.id = id;
        this.name = name;
        this.distance = distance;
        this.date = date;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getDistanceDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getName() {
        return name;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setName(String name) {
        this.name = name;
    }

    /** Not-null value. */
    public String getDistance() {
        return distance;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setDistance(String distance) {
        this.distance = distance;
    }

    /** Not-null value. */
    public java.util.Date getDate() {
        return date;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setDate(java.util.Date date) {
        this.date = date;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    public List<Position> getPositionList() {
        if (positionList == null) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PositionDao targetDao = daoSession.getPositionDao();
            List<Position> positionListNew = targetDao._queryDistance_PositionList(id);
            synchronized (this) {
                if(positionList == null) {
                    positionList = positionListNew;
                }
            }
        }
        return positionList;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    public synchronized void resetPositionList() {
        positionList = null;
    }

    /** Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context. */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    /** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context. */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    /** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context. */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
