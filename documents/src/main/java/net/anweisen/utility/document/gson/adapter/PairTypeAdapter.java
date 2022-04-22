package net.anweisen.utility.document.gson.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.anweisen.utility.common.collection.pair.*;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author anweisen | https://github.com/anweisen
 * @since 1.0
 */
public class PairTypeAdapter implements GsonTypeAdapter<Pair> {

	@Override
	public void write(@Nonnull Gson gson, @Nonnull JsonWriter writer, @Nonnull Pair object) throws IOException {
		Object[] values = object.values();
		JsonArray array = new JsonArray(values.length);
		for (Object value : values) {
			array.add(gson.toJsonTree(value));
		}
	}

	@Override
	public Pair read(@Nonnull Gson gson, @Nonnull JsonReader reader) throws IOException {
		JsonArray array = gson.fromJson(reader, JsonArray.class);
		int size = array.size();
		switch (size) {
			case 1:
				return Wrap.of(array.get(0));
			case 2:
				return Tuple.of(array.get(0), array.get(1));
			case 3:
				return Triple.of(array.get(0), array.get(1), array.get(2));
			case 4:
				return Quadro.of(array.get(0), array.get(1), array.get(2), array.get(3));
			default:
				throw new IllegalStateException("No Pair known for amount of " + size);
		}
	}
}
