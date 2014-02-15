package es.nosys.postgresql.pgconfiguration.pgconfiguration_android.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

public class Param extends es.nosys.postgresql.pgconfiguration.model.Param implements Serializable {

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(getCategory());
		out.writeObject(getContext().name());
		out.writeObject(getDefaultvalue());
		out.writeObject(getDescription());
		out.writeObject(getEnumvalues());
		out.writeObject(getExtra());
		out.writeObject(getMaxval());
		out.writeObject(getMinval());
		out.writeObject(getParam());
		out.writeObject(getUnit());
		out.writeObject(getValue());
		out.writeObject(getVartype().name());
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException {
		try {
			setCategory((String) in.readObject());
			setContext(Context.valueOf((String) in.readObject()));
			setDefaultvalue((String) in.readObject());
			setDescription((String) in.readObject());
			setEnumvalues((List<String>) in.readObject());
			setExtra((String) in.readObject());
			setMaxval((String) in.readObject());
			setMinval((String) in.readObject());
			setParam((String) in.readObject());
			setUnit((String) in.readObject());
			setValue((String) in.readObject());
			setVartype(Vartype.valueOf((String) in.readObject()));
		} catch(ClassNotFoundException classNotFoundException) {
			throw new RuntimeException(classNotFoundException);
		}
	}

}
