package org.uma.jmetal.algorithm.multiobjective.tspmoea;



import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ajnebro on 5/11/14.
 * Modified by Juanjo on 13/11/14
 * This implementation is based on the code of Tsung-Che Chiang
 * http://web.ntnu.edu.tw/~tcchiang/publications/nsga3cpp/nsga3cpp.htm
 *
 */
//这里的S统一  是Solution2,也可以是 Individual，没什么影响, 如果是Individual，会影响attribute吗？
public class ReferencePoint2<Individual>{
  public List<Double> position ;
  private int memberSize ;
  private List<Pair<Individual, Double>> potentialMembers ;

  public ReferencePoint2() {
  }

  /** Constructor */
  public ReferencePoint2(int size) {   //size对应目标个数
    position = new ArrayList<>();
    for (int i =0; i < size; i++)
      position.add(0.0);
    memberSize = 0 ;
    potentialMembers = new ArrayList<>();
  }

  public ReferencePoint2(ReferencePoint2<Individual> point) {
    position = new ArrayList<>(point.position.size());
    for (Double d : point.position) {
      position.add(d);
    }
    memberSize = 0 ;
    potentialMembers = new ArrayList<>();
  }

  public void generateReferencePoints(
          List<ReferencePoint2<Individual>> referencePoints,
          int numberOfObjectives,
          int numberOfDivisions) {

    ReferencePoint2<Individual> refPoint = new ReferencePoint2<>(numberOfObjectives) ;
    generateRecursive(referencePoints, refPoint, numberOfObjectives, numberOfDivisions, numberOfDivisions, 0);
  }

  private void generateRecursive(
          List<ReferencePoint2<Individual>> referencePoints,
          ReferencePoint2<Individual> refPoint,
          int numberOfObjectives,
          int left,
          int total,
          int element) {
    if (element == (numberOfObjectives - 1)) {
      refPoint.position.set(element, (double) left / total) ;
      referencePoints.add(new ReferencePoint2<>(refPoint)) ;
    } else {
      for (int i = 0 ; i <= left; i +=1) {
        refPoint.position.set(element, (double)i/total) ;

        generateRecursive(referencePoints, refPoint, numberOfObjectives, left-i, total, element+1);
      }
    }
  }
  
  public List<Double> pos()  { return this.position; }
  public int  MemberSize(){ return memberSize; }
  public boolean HasPotentialMember() { return potentialMembers.size()>0; }
  public void clear(){ memberSize=0; this.potentialMembers.clear();}
  public void AddMember(){this.memberSize++;}
  public void AddPotentialMember(Individual member_ind, double distance){
    this.potentialMembers.add(new ImmutablePair<Individual,Double>(member_ind,distance) );
  }

  public void sort() {
    this.potentialMembers.sort(Comparator.comparing(Pair<Individual, Double>::getRight).reversed());
  }

  public Individual FindClosestMember() {
    return this.potentialMembers.remove(this.potentialMembers.size() - 1)
            .getLeft();
  }
  
  public Individual RandomMember() {
    int index = this.potentialMembers.size()>1 ? JMetalRandom.getInstance().nextInt(0, this.potentialMembers.size()-1):0;
    return this.potentialMembers.remove(index).getLeft();
  }
}
