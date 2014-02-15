package es.nosys.postgresql.pgconfiguration.pgconfiguration_android.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

public class Configuration extends
		es.nosys.postgresql.pgconfiguration.model.Configuration implements
		Serializable {

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeObject(getPostgresqlconf());
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException {
		try {
			setPostgresqlconf(
					(List<es.nosys.postgresql.pgconfiguration.model.Param>) in.readObject());
		} catch(ClassNotFoundException classNotFoundException) {
			throw new RuntimeException(classNotFoundException);
		}
	}
}
