package de.symeda.sormas.api.systemevents;

import de.symeda.sormas.api.i18n.I18nProperties;

public enum SystemEventStatus {

	SUCCESS,
	ERROR;

	public String toString() {
		return I18nProperties.getEnumCaption(this);
	}
}