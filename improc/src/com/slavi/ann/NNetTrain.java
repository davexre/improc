package com.slavi.ann;

import java.util.ArrayList;

public class NNetTrain {

  protected boolean learning = false;
  
  protected boolean aborting = false;
  
  protected int iteration = 0;
  
  protected int currentItem = -1;

  public ArrayList items = new ArrayList();
  
  public boolean isAborting() {
    return aborting;
  }

  public void AbortLearning() {
    if (learning)
      this.aborting = true;
  }

  public int getCurrentItem() {
    return currentItem;
  }

  public int getIteration() {
    return iteration;
  }

  public boolean isLearning() {
    return learning;
  }
  
  public void learn(NNet net, int maxIterations) {
    double[] er = net.getOutput();
    learning = true;
    aborting = false;
    iteration = 0;
    currentItem = 0;

    while ((iteration < maxIterations) && (!aborting)) {
      for (currentItem = items.size() - 1; 
          (currentItem >= 0) && (!aborting); 
          currentItem--) {
        double[] op;
        NNetLearnPair p = (NNetLearnPair)items.get(currentItem);
        net.feedForward(p.getInputPattern());
        op = p.getOutputPattern();
        for (int i = er.length - 1; i >= 0; i--)
          er[i] = op[i] - er[i];
        net.backPropagate(er);
      }
    }
    learning = false;    
  }
}
