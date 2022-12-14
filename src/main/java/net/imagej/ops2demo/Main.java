package net.imagej.ops2demo;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.BiFunction;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.discovery.Discoverer;
import org.scijava.function.Computers;
import org.scijava.function.Functions;
import org.scijava.function.Inplaces;
import org.scijava.ops.api.OpEnvironment;
import org.scijava.ops.api.OpInfo;
import org.scijava.ops.engine.DefaultOpEnvironment;
import org.scijava.types.Nil;

public class Main {
	public static void main(String[] args) {
		Img<UnsignedByteType> clown = ArrayImgs.unsignedBytes(256, 192);

		double[] sigmas = {20, 5};

		//OpEnvironment ops = new DefaultOpEnvironment();
		OpEnvironment ops = new DefaultOpEnvironment(Discoverer.all(ServiceLoader::load));


		List<OpInfo> infosList = new ArrayList<>();
		for (OpInfo info : ops.infos()) {
			infosList.add(info);
			System.out.println(info);
		}
		System.out.println("Found " + infosList.size() + " ops total!");

		// function = apply
		// computer = compute
		// inplace = mutate

		Img<DoubleType> result = ArrayImgs.doubles(256, 256);

		// Function: Do the gauss on clown and return the result, agnostic of output type.
		Object resultUnknownType = ops.op("filter.gauss")
				.input(clown, sigmas)
				.apply(); // match AND execute as a function

		// Function: Do the gauss on clown and return the result, as an Img.
		Img resultDataset = ops.op("filter.gauss")
				.input(clown, sigmas)
				.outType(Img.class) // only match ops whose output is compatible with Img
				.apply(); // match AND execute as a function

		// Function: Do the gauss on clown and return the result, as an Img<DoubleType>.
		Img<DoubleType> resultDoubleTypeImg = ops.op("filter.gauss")
				.input(clown, sigmas)
				.outType(new Nil<Img<DoubleType>>() {}) // only match ops whose output is compatible with Img<DoubleType> generic type
				.apply(); // match AND execute as a function

		// Or match once and then reuse the function many times:
		BiFunction<Img<UnsignedByteType>, double[], Img<DoubleType>> gaussOp =
			ops.op("filter.gauss")
				.input(clown, sigmas)
				.outType(new Nil<Img<DoubleType>>() {})
				.function(); // match and return the function -- do not execute immediately
		for (int i = 0; i < 10; i++) {
			sigmas[0]++;
			Img<DoubleType> blurrier = gaussOp.apply(clown, sigmas);
		}

		// Ternary function instead of binary
		Functions.Arity3<Img<UnsignedByteType>, double[], String, Img<DoubleType>> fastGaussOp =
			ops.op("filter.gauss")
				.input(clown, sigmas, "fast")
				.outType(new Nil<Img<DoubleType>>() {})
				.function();

		// Function: In case I have no actual objects yet, I only know their types
		BiFunction<Img<UnsignedByteType>, double[], Img<DoubleType>> sameGaussOpAsBefore =
			ops.op("filter.gauss")
					.inType(new Nil<Img<UnsignedByteType>>() {}, new Nil<double[]>() {}) // I only have the type, not actual object instances
					.outType(new Nil<Img<DoubleType>>() {})
					.function();

		// Computer: Do the gauss on clown and store result into result container.
		ops.op("filter.gauss").input(clown, sigmas).output(result).compute();

		Computers.Arity2<Img<UnsignedByteType>, double[], Img<DoubleType>> gaussComputer = ops.op("filter.gauss").input(clown, sigmas).output(result).computer();

		// Inplace2_1: Do the gauss on clown inplace
		ops.op("filter.gauss").input(clown, sigmas).mutate1();

		Inplaces.Arity2_1<Img<UnsignedByteType>, double[]> inplaceGaussOp = ops.op("filter.gauss").input(clown, sigmas).inplace1();
	}
}
