package com.slavi.math.transform;

import java.util.Map;

import com.slavi.math.adjust.LeastSquaresAdjust;
import com.slavi.math.adjust.Statistics;
import com.slavi.math.matrix.Matrix;

/*

 TX' =  a * SX + b * SY + c
 TY' = -b * SX + a * SY + d

 dX = TX' - TX
 dY = TY' - TY

 Adjust a,b,c,d so that the distance F = Math.Sqrt(dX*dX + dY*dY) = min
 
 */
public abstract class Helmert2DTransformLearner2<InputType, OutputType> extends BaseTransformLearner<InputType, OutputType> {

	protected Matrix coefs;			// Temporary matrix, used by computeOne methods
	protected LeastSquaresAdjust lsa;

	public Helmert2DTransformLearner2(Helmert2DTransformer2<InputType, OutputType> transformer, 
			Iterable<? extends Map.Entry<InputType, OutputType>> pointsPairList) {
		super(transformer, pointsPairList);

		this.coefs = new Matrix(4, 1);
		this.lsa = new LeastSquaresAdjust(4, 1);
	}
	
	public int getRequiredTrainingPoints() {
		return 4;
	}	
	
	public void calculatePrims() {
		computeScaleAndOrigin();
		Helmert2DTransformer2<InputType, OutputType> tr = (Helmert2DTransformer2<InputType, OutputType>) transformer;
		double c = 0.5 * (
				targetMax.getItem(0, 0) + targetMin.getItem(0, 0)  
				- sourceMax.getItem(0, 0) - sourceMin.getItem(0, 0));
		double d = 0.5 * (
				targetMax.getItem(1, 0) + targetMin.getItem(1, 0)  
				- sourceMax.getItem(1, 0) - sourceMin.getItem(1, 0));
		Statistics stat = new Statistics();
		stat.start();
		for (Map.Entry<InputType, OutputType> item : items) {
			if (isBad(item))
				continue;
			InputType source = item.getKey();
			OutputType target = item.getValue();
			double dx = tr.getTargetCoord(target, 0) - (tr.getSourceCoord(source, 0) + c);
			double dy = tr.getTargetCoord(target, 1) - (tr.getSourceCoord(source, 1) + d);
			double angle = Math.atan2(dy, dx);
			stat.addValue(angle);
//			stat.addValue(angle, getWeight(item));
		}
		stat.stop();
		double angle = stat.getA();
		tr.a = Math.cos(angle);
		tr.b = Math.sin(angle);
		tr.c = c;
		tr.d = d;
	}

	/**
	 * 
	 * @return True if adjusted. False - Try again/more adjustments needed.
	 */
	public TransformLearnerResult calculateOne() {
		TransformLearnerResult result = new TransformLearnerResult();
		result.minGoodRequired = lsa.getRequiredPoints();
		computeWeights(result);
		if (result.oldGoodCount < result.minGoodRequired)
			return result;

		Helmert2DTransformer2<InputType, OutputType> tr = (Helmert2DTransformer2<InputType, OutputType>)transformer;
		// Calculate the affine transform parameters 
		lsa.clear();
		for (Map.Entry<InputType, OutputType> item : items) {
			if (isBad(item))
				continue;
			InputType source = item.getKey();
			OutputType target = item.getValue();
			double computedWeight = getComputedWeight(item);
			
			double SX = transformer.getSourceCoord(source, 0);
			double SY = transformer.getSourceCoord(source, 1);
			double TX = transformer.getTargetCoord(target, 0);
			double TY = transformer.getTargetCoord(target, 1);
			
			double B =  tr.a * SX + tr.b * SY + tr.c - TX;
			double C = -tr.b * SX + tr.a * SY + tr.d - TY;
			double F = B*B + C*C;
			double dF_da = 2.0 * (SX * B + SY * C); 
			double dF_db = 2.0 * (SY * B - SX * C); 
			double dF_dc = 2.0 * B; 
			double dF_dd = 2.0 * C; 
			
			coefs.setItem(0, 0, dF_da);
			coefs.setItem(1, 0, dF_db);
			coefs.setItem(2, 0, dF_dc);
			coefs.setItem(3, 0, dF_dd);
			lsa.addMeasurement(coefs, computedWeight, F, 0);
		}
		if (!lsa.calculate())
			return result;

		// Build transformer
		Matrix u = lsa.getUnknown(); 

		tr.a -= u.getItem(0, 0);
		tr.b -= u.getItem(0, 1);
		tr.c -= u.getItem(0, 2); 
		tr.d -= u.getItem(0, 3);
		
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result; 
	}
}
