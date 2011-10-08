package com.mongodb;

import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.bson.types.ObjectId;

import java.util.*;

public class FakeDBCollection extends DBCollection {

    public FakeDBCollection(FakeDB base, String name) {
        super(base, name);
    }

    public List<DBObject> getObjects() {
        return objects;
    }

    @Override
    public WriteResult insert(DBObject[] arr, WriteConcern concern) throws MongoException {
        if (arr == null) {
            return null;
        }
        List<DBObject> dbObjects = Arrays.asList(arr);
        objects.addAll(dbObjects);
        for (DBObject dbObject : dbObjects) {
            if (dbObject.get("_id") == null) {
                dbObject.put("_id", ObjectId.get());
            }
        }
        return null;
    }

    @Override
    public WriteResult update(DBObject q, DBObject o, boolean upsert, boolean multi, WriteConcern concern) throws MongoException {
        lastUpdate = o;
        DBObject old = findOne(q);
        objects.remove(old);
        objects.add(o);
        return null;
    }

    @Override
    protected void doapply(DBObject o) {
    }

    @Override
    public WriteResult remove(DBObject o, WriteConcern concern) throws MongoException {
        objects.remove(o);
        return null;
    }

    @Override
    Iterator<DBObject> __find(DBObject ref, DBObject fields, int numToSkip, int batchSize, int limit, int options) throws MongoException {
        final Object id = ref.get("_id");
        if (id == null) {
            return returnAll(numToSkip, limit);
        }
        return findById(id);
    }

    private Iterator<DBObject> findById(final Object id) {
        DBObject dbObject = null;
        try {
            dbObject = Iterables.find(objects, new Predicate<DBObject>() {
                @Override
                public boolean apply(DBObject input) {
                    return input.get("_id").equals(id);
                }
            });
        } catch (Exception e) {
            return null;
        }
        return Lists.newArrayList(dbObject).iterator();
    }

    private Iterator<DBObject> returnAll(int skip, int limit) {
        List<DBObject> result = objects;
        if(skip > 0) {
            result =  Lists.newArrayList(Iterables.skip(result, skip));
        }
        if(limit > 0) {
            return Iterables.limit(result, limit).iterator();
        }
        return result.iterator();
    }

    @Override
    public void createIndex(DBObject keys, DBObject options) throws MongoException {
    }

    @Override
    public long count() throws MongoException {
        return objects.size();
    }

    public DBObject lastUpdate() {
        return lastUpdate;
    }

    private final List<DBObject> objects = Lists.newArrayList();
    private DBObject lastUpdate;
}
