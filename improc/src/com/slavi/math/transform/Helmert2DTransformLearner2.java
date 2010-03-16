package com.slavi.math.transform;

import java.util.Map;

import com.slavi.math.MathUtil;
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
		
//		c = 0.5 * (targetMin.getItem(0, 0) + sourceMin.getItem(0, 0));
//		d = 0.5 * (targetMin.getItem(1, 0) + sourceMin.getItem(1, 0));
		
		Statistics statA = new Statistics();
		Statistics statB = new Statistics();
		statA.start();
		statB.start();
		for (Map.Entry<InputType, OutputType> item : items) {
			if (isBad(item))
				continue;
			InputType source = item.getKey();
			OutputType target = item.getValue();
			double a = tr.getTargetCoord(target, 0) - (tr.getSourceCoord(source, 0) + c);
			double b = tr.getTargetCoord(target, 1) - (tr.getSourceCoord(source, 1) + d);
//			System.out.println("A=" + a + "\tB=" + b);
			statA.addValue(a);
			statB.addValue(b);
//			stat.addValue(angle, getWeight(item));
		}
		statA.stop();
		statB.stop();
		System.out.println("StatA");
		System.out.println(statA);
		System.out.println("StatB");
		System.out.println(statB);
		tr.a = statA.getAvgValue();
		tr.b = statB.getAvgValue();
		tr.c = c;
		tr.d = d;
/*		
		tr.a = 0;
		tr.b = 0;
		tr.c = 0;
		tr.d = 0;*/
	}

	public TransformLearnerResult calculateTwo() {
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
			double computedWeight = 1.0; //getComputedWeight(item);
			
			double SX = transformer.getSourceCoord(source, 0);
			double SY = transformer.getSourceCoord(source, 1);
			double TX = transformer.getTargetCoord(target, 0);
			double TY = transformer.getTargetCoord(target, 1);
			double L = tr.a * SX + tr.b * SY + tr.c - TX;
			coefs.setItem(0, 0, SX);
			coefs.setItem(1, 0, SY);
			coefs.setItem(2, 0, 1);
			coefs.setItem(3, 0, 0);
			lsa.addMeasurement(coefs, computedWeight, L, 0);

			L = -tr.b * SX + tr.a * SY + tr.d - TY;
			coefs.setItem(0, 0, SY);
			coefs.setItem(1, 0, -SX);
			coefs.setItem(2, 0, 0);
			coefs.setItem(3, 0, 1);
			lsa.addMeasurement(coefs, computedWeight, L, 0);
		}
		if (!lsa.calculate())
			return result;
		
		// Build transformer
		Matrix u = lsa.getUnknown();
		u.rMul(-1);
		u.printM("U");
		
		tr.a += u.getItem(0, 0);
		tr.b += u.getItem(0, 1);
		tr.c += u.getItem(0, 2); 
		tr.d += u.getItem(0, 3);
		
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result; 
	}
	
	public TransformLearnerResult calculateOLD() {
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
			double L = TX;
			coefs.setItem(0, 0, SX);
			coefs.setItem(1, 0, SY);
			coefs.setItem(2, 0, 1);
			coefs.setItem(3, 0, 0);
			lsa.addMeasurement(coefs, computedWeight, L, 0);
			
			L = TY;
			coefs.setItem(0, 0, SY);
			coefs.setItem(1, 0, -SX);
			coefs.setItem(2, 0, 0);
			coefs.setItem(3, 0, 1);
			lsa.addMeasurement(coefs, computedWeight, L, 0);
		}
		if (!lsa.calculate())
			return result;
		
		// Build transformer
		Matrix u = lsa.getUnknown();
		u.printM("U");
		
		tr.a = u.getItem(0, 0);
		tr.b = u.getItem(0, 1);
		tr.c = u.getItem(0, 2); 
		tr.d = u.getItem(0, 3);
		
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result; 
	}

	/**
	 * 
	 * @return True if adjusted. False - Try again/more adjustments needed.
	 */
	public TransformLearnerResult calculateOne1() {
		TransformLearnerResult result = new TransformLearnerResult();
		result.minGoodRequired = lsa.getRequiredPoints();
		computeWeights(result);
		if (result.oldGoodCount < result.minGoodRequired)
			return result;
		computeScaleAndOrigin();
		double SOX = sourceMin.getItem(0, 0);
		double SOY = sourceMin.getItem(1, 0);
		
		double TOX = targetMin.getItem(0, 0);
		double TOY = targetMin.getItem(1, 0);
		
		double SS = Math.max(sourceMax.getItem(0, 0) - SOX, sourceMax.getItem(1, 0) - SOY) / 2.0;
		double TS = Math.max(targetMax.getItem(0, 0) - TOX, targetMax.getItem(1, 0) - TOY) / 2.0;
		
		Helmert2DTransformer2<InputType, OutputType> tr = (Helmert2DTransformer2<InputType, OutputType>)transformer;
		OutputType sourceTransformed = createTemporaryTargetObject();
		// Calculate the affine transform parameters 
		lsa.clear();
		double tr_a = tr.a * SS / TS;
		double tr_b = tr.b * SS / TS;
		double tr_c = (tr.c - TOX + tr.a * SOX + tr.b * SOY) / TS;
		double tr_d = (tr.d - TOY - tr.b * SOX + tr.a * SOY) / TS;
		
		System.out.println("tr_a=" + MathUtil.d4(tr_a));
		System.out.println("tr_b=" + MathUtil.d4(tr_b));
		System.out.println("tr_c=" + MathUtil.d4(tr_c));
		System.out.println("tr_d=" + MathUtil.d4(tr_d));
		
		double scale = Math.sqrt(tr_a*tr_a + tr_b*tr_b);
		double cosA = tr_a / scale;
		double sinA = tr_b / scale;
		double Angle = Math.atan2(cosA, sinA);
		
		for (Map.Entry<InputType, OutputType> item : items) {
			if (isBad(item))
				continue;
			InputType source = item.getKey();
			OutputType target = item.getValue();
			double computedWeight = 1.0; //getComputedWeight(item);
			
			double SX = transformer.getSourceCoord(source, 0);
			double SY = transformer.getSourceCoord(source, 1);
			double TX = transformer.getTargetCoord(target, 0);
			double TY = transformer.getTargetCoord(target, 1);
			
			double sx = (SX - SOX) / SS;
			double sy = (SY - SOY) / SS;
			double tx = (TX - TOX) / TS;
			double ty = (TY - TOY) / TS;
						
			
			double B =  tr_a * sx + tr_b * sy + tr_c - tx;
			double C = -tr_b * sx + tr_a * sy + tr_d - ty;
			
			double F = Math.sqrt(B*B + C*C);
			if (F == 0.0)
				//throw new Error("0");
				continue;
			double dF_ds = ((cosA * sx +  sinA * sy) * B + (cosA * sy - sinA * sx) * C) / F; 
			double dF_dA = ((scale * (-sinA) * sx + scale * cosA * sy) * B + (-scale * cosA * sx - scale * sinA * sy) * C) / F; 
			double dF_dc = B / F;
			double dF_dd = C / F;
			
			coefs.setItem(0, 0, dF_ds);
			coefs.setItem(1, 0, dF_dA);
			coefs.setItem(2, 0, dF_dc);
			coefs.setItem(3, 0, dF_dd);
			lsa.addMeasurement(coefs, computedWeight, F, 0);
/*
			if (true) {
				double F = Math.sqrt(B*B + C*C);
				if (F == 0.0)
					throw new Error("0");
				double dF_da = (SX * B + SY * C); 
				double dF_db = (SY * B - SX * C); 
				double dF_dc = B; 
				double dF_dd = C; 
				
				coefs.setItem(0, 0, dF_da);
				coefs.setItem(1, 0, dF_db);
				coefs.setItem(2, 0, dF_dc);
				coefs.setItem(3, 0, dF_dd);
//				System.out.print(MathUtil.d4(F) + "\t" + coefs);
				lsa.addMeasurement(coefs, computedWeight, F, 0);
			} else {
				double F = B*B;
				double dF_da = 2.0 * (SX * B); 
				double dF_db = 2.0 * (SY * B); 
				double dF_dc = 2.0 * B; 
				double dF_dd = 0; 
	
				coefs.setItem(0, 0, dF_da);
				coefs.setItem(1, 0, dF_db);
				coefs.setItem(2, 0, dF_dc);
				coefs.setItem(3, 0, dF_dd);
//				System.out.print(MathUtil.d4(F) + "\t" + coefs);
				lsa.addMeasurement(coefs, computedWeight, F, 0);

				F = C*C;
				dF_da = 2.0 * (SY * C); 
				dF_db = 2.0 * (- SX * C); 
				dF_dc = 0; 
				dF_dd = 2.0 * C; 
				
				coefs.setItem(0, 0, dF_da);
				coefs.setItem(1, 0, dF_db);
				coefs.setItem(2, 0, dF_dc);
				coefs.setItem(3, 0, dF_dd);
//				System.out.print(MathUtil.d4(F) + "\t" + coefs);
				lsa.addMeasurement(coefs, computedWeight, F, 0);
			}*/
/*			transformer.transform(source, sourceTransformed);
			double d1 = transformer.getTargetCoord(sourceTransformed, 0) - transformer.getTargetCoord(target, 0);
			double d2 = transformer.getTargetCoord(sourceTransformed, 1) - transformer.getTargetCoord(target, 1);
			double d = d1 * d1 + d2 * d2;
			if (d != F)
				System.out.println("d=" + d + "\tF=" + F);*/
		}
		System.out.println("det=" + lsa.getNm().makeSquareMatrix().det());
		lsa.getNm().makeSquareMatrix().printM("NM");
		if (!lsa.calculate())
			return result;
		lsa.getNm().makeSquareMatrix().printM("NM'");

		// Build transformer
		Matrix u = lsa.getUnknown();
		u.rMul(-1);
		u.printM("U");
	
		scale += u.getItem(0, 0);
		Angle += u.getItem(0, 1);
		tr_a = scale * Math.cos(Angle);
		tr_b = scale * Math.sin(Angle);
		tr_c += u.getItem(0, 2); 
		tr_d += u.getItem(0, 3);

		System.out.println("tr_a=" + MathUtil.d4(tr_a));
		System.out.println("tr_b=" + MathUtil.d4(tr_b));
		System.out.println("tr_c=" + MathUtil.d4(tr_c));
		System.out.println("tr_d=" + MathUtil.d4(tr_d));
		
		tr.a = tr_a * TS / SS;
		tr.b = tr_b * TS / SS;
		tr.d = tr_d * TS + TOY + tr.b * SOX - tr.a * SOY;
		tr.c = tr_c * TS + TOX - tr.a * SOX - tr.b * SOY;
		
/*		tr.a += u.getItem(0, 0);
		tr.b += u.getItem(0, 1);
		tr.c += u.getItem(0, 2); 
		tr.d += u.getItem(0, 3);
*/		

		System.out.println("A=" + MathUtil.d4(tr.a));
		System.out.println("B=" + MathUtil.d4(tr.b));
		System.out.println("C=" + MathUtil.d4(tr.c));
		System.out.println("D=" + MathUtil.d4(tr.d));
		
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result; 
	}

	public TransformLearnerResult calculateOne() {
		TransformLearnerResult result = new TransformLearnerResult();
		result.minGoodRequired = lsa.getRequiredPoints();
		computeWeights(result);
		if (result.oldGoodCount < result.minGoodRequired)
			return result;
		Helmert2DTransformer2<InputType, OutputType> tr = (Helmert2DTransformer2<InputType, OutputType>)transformer;
		// Calculate the affine transform parameters 
		lsa.clear();
		computeScaleAndOrigin();
		
		double scaleX = sourceScale.getItem(0, 0);
		double scaleY = scaleX; //sourceScale.getItem(1, 0);
		
		for (Map.Entry<InputType, OutputType> item : items) {
			if (isBad(item))
				continue;
			InputType source = item.getKey();
			OutputType target = item.getValue();
			double computedWeight = 1.0; //getComputedWeight(item);
			
			double SX = transformer.getSourceCoord(source, 0);
			double SY = transformer.getSourceCoord(source, 1);
			double TX = transformer.getTargetCoord(target, 0);
			double TY = transformer.getTargetCoord(target, 1);

			double DX = tr.a * SX + tr.b * SY + tr.c - TX;
			double DY =-tr.b * SX + tr.a * SY + tr.d - TY;
			double F = DX*DX + DY*DY;
			if (F == 0.0) {
				//throw new Error("0");
				System.out.println("Got F=0");
				continue;
			}
			double dF_da = DX * SX + DY * SY; 
			double dF_db = DX * SY - DY * SX; 
			double dF_dc = DX;
			double dF_dd = DY;
			
			dF_da *= scaleX;
			dF_db *= scaleY;
			
			System.out.println(F + "\t" + dF_da + "\t" + dF_db + "\t" + dF_dc + "\t" + dF_dd);
			
			coefs.setItem(0, 0, dF_da);
			coefs.setItem(1, 0, dF_db);
			coefs.setItem(2, 0, dF_dc);
			coefs.setItem(3, 0, dF_dd);
			lsa.addMeasurement(coefs, computedWeight, F, 0);
		}
		System.out.println("det=" + lsa.getNm().makeSquareMatrix().det());
		Matrix nm = lsa.getNm().makeSquareMatrix();
//		nm.printM("NM");
		if (!lsa.calculate())
			return result;
		Matrix nm_inv = lsa.getNm().makeSquareMatrix();
//		nm_inv.printM("NM'");
		Matrix check = new Matrix();
		nm.mMul(nm_inv, check);
//		check.printM("nm*nm'");
		double sumAbs = check.sumAbs();
		System.out.println("sumAbs=" + sumAbs);
		if (Math.abs(sumAbs - check.getSizeX()) > 1)
			throw new RuntimeException("");

		// Build transformer
		Matrix u = lsa.getUnknown();
		u.rMul(-1);
		u.printM("U");
	
		tr.a += u.getItem(0, 0) / scaleX;
		tr.b += u.getItem(0, 1) / scaleY;
		tr.d += u.getItem(0, 2);
		tr.c += u.getItem(0, 3);
		
		computeDiscrepancies(result);
		computeBad(result);
		result.adjustFailed = false;
		return result; 
	}
	
	public void testDerivative(double a, double b, double c, double d) {
		Helmert2DTransformer2<InputType, OutputType> tr = (Helmert2DTransformer2<InputType, OutputType>)transformer;
		for (Map.Entry<InputType, OutputType> item : items) {
			InputType source = item.getKey();
			OutputType target = item.getValue();

			double SX = transformer.getSourceCoord(source, 0);
			double SY = transformer.getSourceCoord(source, 1);
			double TX = transformer.getTargetCoord(target, 0);
			double TY = transformer.getTargetCoord(target, 1);
			
			double DX0 = a * SX + b * SY + c - TX;
			double DY0 =-b * SX + a * SY + d - TY;
			double F0 = DX0*DX0 + DY0*DY0;
						
			double DX = tr.a * SX + tr.b * SY + tr.c - TX;
			double DY =-tr.b * SX + tr.a * SY + tr.d - TY;
			double F = DX*DX + DY*DY;

			double dF_da = DX * SX + DY * SY; 
			double dF_db = DX * SY - DY * SX; 
			double dF_dc = DX;
			double dF_dd = DY;
			
			double F1 = F 
				+ dF_da * (a - tr.a)
				+ dF_db * (b - tr.b)
				+ dF_dc * (c - tr.c)
				+ dF_dd * (d - tr.d);
			System.out.println(isBad(item) + "\t" + MathUtil.d20(F1 - F0)
				+ "\t" + MathUtil.d20(Math.sqrt(F))
				+ "\t" + MathUtil.d20(Math.sqrt(F0)) );
		}		
	}	
}
