package com.threeDBJ.calcAppLib;

import android.app.Application;
import android.util.Log;
import android.content.Intent;
import android.content.Context;
import java.util.ArrayList;
import android.widget.ArrayAdapter;
import java.math.RoundingMode;
import android.net.Uri;

import cliCalc.*;

public class CalcApp extends Application {

    Calculator mainCalc;
    GraphView graph;

    Calculator[] graphCalcs;
    int[] graphIndex;

    float[][] zeros;
    int traceLoc, fnState;
    Calculator copyCalc;

    @Override
    public void onCreate () {
        mainCalc = new Calculator ();
        copyCalc = new Calculator ();
        graphCalcs = new Calculator[3];
        graphIndex = new int[3];
        for (int i=0;i<3;i+=1) {
            graphCalcs[i] = new Calculator ();
            graphIndex[i] = 0;
        }
        zeros = new float[3][128];
        fnState = 0;
    }

    public Calculator getMainCalc () {
        return mainCalc;
    }

    public int getGraphIndex (int ind) {
        return graphIndex[ind];
    }

    public void setGraphIndex (int gind, int ind) {
        graphIndex[gind] = ind;
    }

    public Calculator[] getGraphCalcs () {
        return graphCalcs;
    }

    public void setGraphView (GraphView graph) {
        this.graph = graph;
    }

    void clearArr (float[] arr) {
        for (int i=0;i<arr.length;i+=1) {
            arr[i] = 0;
        }
    }

    public void plotFns () {
        plotFns (getFnPts (), getNumFns ());
    }

    public void initFns () {
        initFns ( getNumFns ());
    }

    public void initFns (int nFns) {
        for (int i=0;i<nFns;i+=1) {
            if (!graphCalcs[i].empty ()) {
                graphCalcs[i].initForGraph ();
            }
        }
    }

    public void resetCalcs (int nFns) {
        for (int i=0;i<nFns;i+=1) {
            if (!graphCalcs[i].empty ()) {
                graphCalcs[i].initForFnEntry ();
            }
        }
    }

    public void plotFns (float [][] fnPts, int nFns) {
        for (int i=0;i<nFns;i+=1) {
            if (!graphCalcs[i].empty ()) {
                /*
                Log.v ("plotFns",Float.toString(graph.getXMax())+" "+
                       Float.toString(graph.getYMax())+" "+Float.toString(graph.getXLeft ())+
                       " "+Float.toString(graph.getXRight ())+" "+
                       Float.toString(graph.getYBot ())+" "+Float.toString(graph.getYTop ()));
                */
                graphCalcs[i].graphFn (fnPts[i], graph.getXLeft (),graph.getXRight(),
                                       graph.getYBot (),graph.getYTop (),
                                       graph.getXMin (),graph.getXMax (),
                                       graph.getYMin (),graph.getYMax (),
                                       graph.getXUnitLen());
                /*
                for (int j=0;j<N_FN_PTS*4;j+=4) {
                    Log.v ("plotFns", Float.toString(fnPts[i][j])+","+Float.toString(fnPts[i][j+1])+
                           "   "+ Float.toString(fnPts[i][j+2])+","+Float.toString(fnPts[i][j+3]));
                }
                */
            } else {
                clearArr (fnPts[i]);
            }
        }
    }

    public void calcZeros (ArrayAdapter<String> zerosArr, int nFns) {
        int n;
        String title;
        for (int i=0;i<nFns;i+=1) {
            if (!graphCalcs[i].empty ()) {
                title = "Fn"+Integer.toString(i+1)+"(x):";
                n = graphCalcs[i].calcZeros (zeros[i], graph.getXLeft (),graph.getXRight(),
                                             graph.getYBot (),graph.getYTop (),
                                             graph.getXMin (),graph.getXMax (),
                                             graph.getYMin (),graph.getYMax (),
                                             graph.getXUnitLen());
                //Log.v ("calcZeros",Integer.toString(n));
                if (n == 0)
                    title += " None";
                zerosArr.add (title);
                // Convert x values of zeros to strings
                for (int k=0;k<n;k+=1) {
                    float num=zeros[i][k];
                    if ((num > -0.001 && num < 0) ||
                        (num < 0.001 && num > 0))
                        num = 0;
                    String numStr = ComplexNumber.roundStr (num, 3);
                    zerosArr.add ("   x = "+numStr);
                }
            }
        }
    }

    public void initTrace () {
        for (int i=0;i<getNumFns();i+=1) {
            traceLoc = (int)((graph.getXMin () + graph.getXMax ()) / 2);
        }
    }

    public void updateTrace (int dx) {
        for (int i=0;i<getNumFns();i+=1) {
            traceLoc += dx;
            if (traceLoc > graph.getXMax ())
                traceLoc = (int)graph.getXMax ();
            else if (traceLoc < graph.getXMin ())
                traceLoc = (int)graph.getXMin ();
        }
    }

    public float getYTracePt (int i, float xval) {
        return graphCalcs[i].getTracePt (xval);
    }

    public int getYGraphPt (float val) {
        return (int)graph.getYMax () -
            graphCalcs[0].realToGraph (val, graph.getYBot (), graph.getYTop (),
                                       graph.getYMin (), graph.getYMax());
    }

    public Calculator getGraphCalc (int ind) {
        return graphCalcs[ind];
    }

    public boolean isCalcEmpty (int i) {
        return graphCalcs[i].empty ();
    }

    public int getNumFns () {
        return graph.NUM_FNS;
    }
    /*
    public int getNumFnPts () {
        return N_FN_PTS;
    }
    */
    public float[][] getFnPts () {
        return graph.fnPts;
    }

    public void getCopy (Calculator calc) {
        calc.viewStr = copyCalc.viewStr;
        calc.tokens = new ArrayList<CalcItem>(copyCalc.tokens);
        calc.tokenLens = new ArrayList<Integer>(copyCalc.tokenLens);
        calc.viewIndex = copyCalc.viewIndex;
    }

    public void setCopy (Calculator calc) {
        copyCalc.viewStr = calc.viewStr;
        copyCalc.tokens = new ArrayList<CalcItem>(calc.tokens);
        copyCalc.tokenLens = new ArrayList<Integer>(calc.tokenLens);
        copyCalc.viewIndex = calc.viewIndex;
    }

    public double getCopyVal () {
        return copyCalc.getValue ();
    }

    public void setCopy (String val) {
        copyCalc.viewStr = val;
        copyCalc.tokens = new ArrayList<CalcItem>();
        copyCalc.tokenLens = new ArrayList<Integer>();
        for (int i=0;i<val.length ();i+=1) {
            copyCalc.tokens.add (new ComplexNumberStr (val.substring (i,i+1)));
            copyCalc.tokenLens.add (1);
        }
        copyCalc.viewIndex = val.length ();
    }

    public void showPurchase (Context c) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.threeDBJ.calcApp"));
        c.startActivity(intent);
    }
}