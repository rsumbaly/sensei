package com.sensei.search.nodes;

import java.io.File;

import org.apache.log4j.Logger;

import proj.zoie.api.ZoieVersion;
import proj.zoie.api.indexing.IndexReaderDecorator;
import proj.zoie.api.indexing.ZoieIndexableInterpreter;
import proj.zoie.hourglass.impl.HourGlassScheduler;
import proj.zoie.hourglass.impl.Hourglass;
import proj.zoie.hourglass.impl.HourglassDirectoryManagerFactory;
import proj.zoie.hourglass.impl.HourGlassScheduler.FREQUENCY;
import proj.zoie.impl.indexing.ZoieConfig;

import com.browseengine.bobo.api.BoboIndexReader;

public class SenseiHourglassFactory<T,V extends ZoieVersion> extends SenseiZoieFactory<T,V>
{
  private static Logger log = Logger.getLogger(SenseiHourglassFactory.class);
  protected File _idxDir;
  protected ZoieIndexableInterpreter<T> _interpreter;
  protected IndexReaderDecorator<BoboIndexReader> _indexReaderDecorator;
  // format "ss mm hh" meaning at hh:mm:ss time of the day that we roll forward for DAILY rolling
  // if it is hourly rolling, it means at mm:ss time of the hour that we roll forward
  // if it is MINUTELY, it means at ss seond of the minute that we roll forward.
  private final String schedule;
  private final int trimThreshold;
  private final HourGlassScheduler.FREQUENCY frequency;
  
  protected final ZoieConfig<V> _zoieConfig;
  
  /**
   * @param idxDir the root directory for Hourglass
   * @param interpreter
   * @param indexReaderDecorator
   * @param zoieConfig
   * @param schedule format: "ss mm hh" meaning at hh:mm:ss time of the day that we roll forward for DAILY rolling;
   * if it is hourly rolling, it means at mm:ss time of the hour that we roll forward;
   * if it is MINUTELY, it means at ss seond of the minute that we roll forward.
   * @param trimThreshold the number of units of rolling periods to keep (for DAILY rolling, we keep trimThreshold number of days of data)
   * @param frequency rolling frequency
   */
  public SenseiHourglassFactory(File idxDir, ZoieIndexableInterpreter<T> interpreter, IndexReaderDecorator<BoboIndexReader> indexReaderDecorator,
                                 ZoieConfig<V> zoieConfig,
                                 String schedule,
                                 int trimThreshold,
                                 FREQUENCY frequency)
  {
    _idxDir = idxDir;
    _interpreter = interpreter;
    _indexReaderDecorator = indexReaderDecorator;
    _zoieConfig = zoieConfig;
    this.schedule = schedule;
    this.trimThreshold = trimThreshold;
    this.frequency = frequency;
    log.info("creating " + this.getClass().getName() + " with schedule: " + schedule
        + " frequency: " + frequency
        + " trimThreshold: " + trimThreshold);
  }
  @Override
  public Hourglass<BoboIndexReader,T,V> getZoieInstance(int nodeId,int partitionId)
  {
    File partDir = getPath(nodeId,partitionId);
    if(!partDir.exists())
    {
      partDir.mkdirs();
      log.info("nodeId="+nodeId+", partition=" + partitionId + " does not exist, directory created.");
    }
    // format "ss mm hh" meaning at hh:mm:ss time of the day, we roll forward for DAILY rolling
    // if it is hourly rolling, it means at mm:ss time of the hour, we roll forward
    // if it is MINUTELY, it means at ss seond of the minute, we roll forward.
    HourGlassScheduler scheduler = new HourGlassScheduler(frequency, schedule, trimThreshold);
    HourglassDirectoryManagerFactory<V> dirmgr = new HourglassDirectoryManagerFactory<V>(partDir, scheduler, _zoieConfig.getZoieVersionFactory());
    log.info("creating Hourglass for nodeId: " + nodeId + " partition: " + partitionId);
    return new Hourglass<BoboIndexReader,T,V>(dirmgr, _interpreter, _indexReaderDecorator, _zoieConfig);
  }
  
  // TODO: change to getDirectoryManager
  public File getPath(int nodeId,int partitionId)
  {
    return getPath(_idxDir,nodeId,partitionId);
  }
}
