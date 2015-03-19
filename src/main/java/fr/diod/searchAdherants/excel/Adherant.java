package fr.diod.searchAdherants.excel;

import javax.persistence.Entity;

import com.google.common.base.Objects;

@Entity
public class Adherant {
	String name = "";
	String maidenName = "";
	String firstName = "";
	String birth = "";
	String portable = "";
	String telephone = "";
	String email = "";

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.addValue(name)
				.addValue(maidenName)
				.addValue(firstName)
				.addValue(birth)
				.addValue(portable)
				.addValue(telephone)
				.addValue(email)
				.toString();
	}

	public Adherant setName(String name) {
		if (name == null) {
			this.name = "";
		} else {
			this.name = name;
		}
		return this;
	}

	public Adherant setMaidenName(String maidenName) {
		if (maidenName == null) {
			this.maidenName = "";
		} else {
			this.maidenName = maidenName;
		}
		return this;
	}

	public Adherant setFirstName(String firstName) {
		if (firstName == null) {
			this.firstName = "";
		} else {
			this.firstName = firstName;
		}
		return this;
	}

	public Adherant setBirthDate(String birthDate) {
		if (birthDate == null) {
			this.birth = "";
		} else {
			this.birth = birthDate;
		}
		return this;
	}

	public void setPortable(String portable) {
		if (portable == null) {
			this.portable = "";
		} else {
			this.portable = portable;
		}
	}

	public void setTelephone(String telephone) {
		if (telephone == null) {
			this.telephone = "";
		} else {
			this.telephone = telephone;
		}

	}

	public void setEmail(String email) {
		if (email == null) {
			this.email = "";
		} else {
			this.email = email;
		}
	}

}
