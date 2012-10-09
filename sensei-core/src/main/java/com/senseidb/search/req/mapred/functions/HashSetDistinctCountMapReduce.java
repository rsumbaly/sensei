package com.senseidb.search.req.mapred.functions;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.senseidb.search.req.mapred.CombinerStage;
import com.senseidb.search.req.mapred.FacetCountAccessor;
import com.senseidb.search.req.mapred.FieldAccessor;
import com.senseidb.search.req.mapred.SenseiMapReduce;
import com.senseidb.util.JSONUtil.FastJSONArray;
import com.senseidb.util.JSONUtil.FastJSONObject;

public class HashSetDistinctCountMapReduce implements SenseiMapReduce<HashSet, Integer> {

  private String column;

  @Override
  public void init(JSONObject params) {
    column = params.optString("column");
    if (column == null) {
      throw new IllegalStateException("Column parameter shouldn't be null");
    }
    
  }

  @Override
  public HashSet map(int[] docId, int docIdCount, long[] uids, FieldAccessor accessor, FacetCountAccessor facetCountAccessor) {
    HashSet hashSet = new HashSet(docIdCount);
    for (int i =0; i < docIdCount; i++) {
      hashSet.add(accessor.getLong(column, docId[i]));
    }
    
    return hashSet;
  }

  @Override
  public List<HashSet> combine(List<HashSet> mapResults, CombinerStage combinerStage) {
    if (mapResults.isEmpty()) {
      return mapResults;
    }
    HashSet ret = mapResults.get(0);

    for (int i = 1; i < mapResults.size(); i++) {
     ret.addAll(mapResults.get(i));
    }
    return Arrays.asList(ret);
  }

  @Override
  public Integer reduce(List<HashSet> combineResults) {
    if (combineResults.isEmpty()) {
      return 0;
    }
    HashSet ret = combineResults.get(0);
    for (int i = 1; i < combineResults.size(); i++) {
     ret.addAll(combineResults.get(i));
    }
    
    return ret.size();
  }

  @Override
  public JSONObject render(Integer reduceResult) {
    // TODO Auto-generated method stub
    try {
      return new FastJSONObject().put("distinctCount", reduceResult);
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
  }
}
