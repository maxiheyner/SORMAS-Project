/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.ui.caze;

import java.util.Arrays;
import java.util.List;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.ui.AbstractSelect;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.OptionGroup;
import com.vaadin.v7.ui.TextArea;
import com.vaadin.v7.ui.TextField;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.caze.CaseLogic;
import de.symeda.sormas.api.caze.CaseOrigin;
import de.symeda.sormas.api.caze.CaseOutcome;
import de.symeda.sormas.api.caze.HospitalWardType;
import de.symeda.sormas.api.caze.InvestigationStatus;
import de.symeda.sormas.api.caze.Vaccination;
import de.symeda.sormas.api.caze.classification.DiseaseClassificationCriteriaDto;
import de.symeda.sormas.api.facility.FacilityDto;
import de.symeda.sormas.api.facility.FacilityReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.i18n.Validations;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.person.Sex;
import de.symeda.sormas.api.region.CommunityReferenceDto;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.utils.AbstractEditForm;
import de.symeda.sormas.ui.utils.CommitDiscardWrapperComponent.DoneListener;
import de.symeda.sormas.ui.utils.ConfirmationComponent;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.FieldHelper;
import de.symeda.sormas.ui.utils.LayoutUtil;
import de.symeda.sormas.ui.utils.LayoutUtil.FluidColumn;
import de.symeda.sormas.ui.utils.StringToAngularLocationConverter;
import de.symeda.sormas.ui.utils.VaadinUiUtil;
import de.symeda.sormas.ui.utils.ViewMode;

@SuppressWarnings("serial")
public class CaseDataForm extends AbstractEditForm<CaseDataDto> {

	private static final String MEDICAL_INFORMATION_LOC = "medicalInformationLoc";
	private static final String PAPER_FORM_DATES_LOC = "paperFormDatesLoc";
	private static final String SMALLPOX_VACCINATION_SCAR_IMG = "smallpoxVaccinationScarImg";
	private static final String CLASSIFICATION_RULES_LOC = "classificationRulesLoc";
	private static final String CLASSIFIED_BY_SYSTEM_LOC = "classifiedBySystemLoc";
	private static final String ASSIGN_NEW_EPID_NUMBER_LOC = "assignNewEpidNumberLoc";
	private static final String EPID_NUMBER_WARNING_LOC = "epidNumberWarningLoc";

	public static final String NONE_HEALTH_FACILITY_DETAILS = CaseDataDto.NONE_HEALTH_FACILITY_DETAILS;

	private static final String HTML_LAYOUT = LayoutUtil.h3(I18nProperties.getString(Strings.headingCaseData))
			+ LayoutUtil.fluidRowLocs(4, CaseDataDto.UUID, 3, CaseDataDto.REPORT_DATE, 5, CaseDataDto.REPORTING_USER)
			+ LayoutUtil.inlineLocs(CaseDataDto.CASE_CLASSIFICATION, CLASSIFICATION_RULES_LOC)
			+ LayoutUtil.fluidRow(
					LayoutUtil.fluidColumnLoc(3, 0, CaseDataDto.CLASSIFICATION_DATE),
					LayoutUtil.fluidColumnLocCss(CssStyles.LAYOUT_COL_HIDE_INVSIBLE, 5, 0, CaseDataDto.CLASSIFICATION_USER),
					LayoutUtil.fluidColumnLocCss(CssStyles.LAYOUT_COL_HIDE_INVSIBLE, 4, 0, CLASSIFIED_BY_SYSTEM_LOC))
			+ LayoutUtil.fluidRowLocs(9, CaseDataDto.INVESTIGATION_STATUS, 3, CaseDataDto.INVESTIGATED_DATE)
			+ LayoutUtil.fluidRowLocs(6, CaseDataDto.EPID_NUMBER, 3, ASSIGN_NEW_EPID_NUMBER_LOC)
			+ LayoutUtil.loc(EPID_NUMBER_WARNING_LOC)
			+ LayoutUtil.fluidRowLocs(6, CaseDataDto.EXTERNAL_ID, 6, null)
			+ LayoutUtil.fluidRow(
					new FluidColumn(null, 6, 0, CaseDataDto.DISEASE, null),
					new FluidColumn(null, 6, 0, null,
							LayoutUtil.locs(CaseDataDto.DISEASE_DETAILS, CaseDataDto.PLAGUE_TYPE,
									CaseDataDto.DENGUE_FEVER_TYPE, CaseDataDto.RABIES_TYPE)))
			+ LayoutUtil.fluidRowLocs(9, CaseDataDto.OUTCOME, 3, CaseDataDto.OUTCOME_DATE)
			+ LayoutUtil.fluidRowLocs(3, CaseDataDto.SEQUELAE, 9, CaseDataDto.SEQUELAE_DETAILS)
			+ LayoutUtil.fluidRowLocs(CaseDataDto.CASE_ORIGIN, "")
			+ LayoutUtil.fluidRowLocs(CaseDataDto.REGION, CaseDataDto.DISTRICT)
			+ LayoutUtil.fluidRowLocs(CaseDataDto.COMMUNITY, CaseDataDto.HEALTH_FACILITY)
			+ LayoutUtil.fluidRowLocs(CaseDataDto.HEALTH_FACILITY_DETAILS)
			+ LayoutUtil.fluidRowLocs(CaseDataDto.POINT_OF_ENTRY, CaseDataDto.POINT_OF_ENTRY_DETAILS)
			+ LayoutUtil.fluidRowLocs(CaseDataDto.REPORT_LAT, CaseDataDto.REPORT_LON,
					CaseDataDto.REPORT_LAT_LON_ACCURACY)
			+ LayoutUtil.fluidRowLocs(CaseDataDto.ADDITIONAL_DETAILS)
			+ LayoutUtil.loc(MEDICAL_INFORMATION_LOC)
			+ LayoutUtil.fluidRowLocs(CaseDataDto.PREGNANT, "")
			+ LayoutUtil.fluidRowLocs(CaseDataDto.VACCINATION, CaseDataDto.VACCINATION_DOSES)
			+ LayoutUtil.fluidRowLocs(CaseDataDto.VACCINE, "")
			+ LayoutUtil.fluidRowLocs(CaseDataDto.SMALLPOX_VACCINATION_RECEIVED, CaseDataDto.SMALLPOX_VACCINATION_SCAR)
			+ LayoutUtil.fluidRowLocs(SMALLPOX_VACCINATION_SCAR_IMG)
			+ LayoutUtil.fluidRowLocs(CaseDataDto.VACCINATION_DATE, CaseDataDto.VACCINATION_INFO_SOURCE)
			+ LayoutUtil.fluidRowLocs(CaseDataDto.SURVEILLANCE_OFFICER, CaseDataDto.CLINICIAN_NAME)
			+ LayoutUtil.fluidRowLocs(CaseDataDto.NOTIFYING_CLINIC, CaseDataDto.NOTIFYING_CLINIC_DETAILS)
			+ LayoutUtil.fluidRowLocs(CaseDataDto.CLINICIAN_PHONE, CaseDataDto.CLINICIAN_EMAIL)
			+ LayoutUtil.loc(PAPER_FORM_DATES_LOC)
			+ LayoutUtil.fluidRowLocs(CaseDataDto.DISTRICT_LEVEL_DATE, CaseDataDto.REGION_LEVEL_DATE, CaseDataDto.NATIONAL_LEVEL_DATE);

	private final PersonDto person;
	private final Disease disease;
	private final ViewMode viewMode;
	
	public CaseDataForm(PersonDto person, Disease disease, UserRight editOrCreateUserRight, ViewMode viewMode) {
		super(CaseDataDto.class, CaseDataDto.I18N_PREFIX, editOrCreateUserRight);
		this.person = person;
		this.disease = disease;
		this.viewMode = viewMode;
		addFields();
	}

	@Override
	protected void addFields() {
		if (person == null || disease == null) {
			return;
		}

		// Add fields
		addFields(CaseDataDto.UUID, CaseDataDto.REPORT_DATE, CaseDataDto.REPORTING_USER,
				CaseDataDto.DISTRICT_LEVEL_DATE, CaseDataDto.REGION_LEVEL_DATE, CaseDataDto.NATIONAL_LEVEL_DATE, 
				CaseDataDto.CLASSIFICATION_DATE, CaseDataDto.CLASSIFICATION_USER, CaseDataDto.CLASSIFICATION_COMMENT,
				CaseDataDto.NOTIFYING_CLINIC, CaseDataDto.NOTIFYING_CLINIC_DETAILS, CaseDataDto.CLINICIAN_NAME,
				CaseDataDto.CLINICIAN_PHONE, CaseDataDto.CLINICIAN_EMAIL);
		
		// Button to automatically assign a new epid number
		Button assignNewEpidNumberButton = new Button(I18nProperties.getCaption(Captions.actionAssignNewEpidNumber));
		CssStyles.style(assignNewEpidNumberButton, ValoTheme.BUTTON_DANGER, CssStyles.FORCE_CAPTION);
		getContent().addComponent(assignNewEpidNumberButton, ASSIGN_NEW_EPID_NUMBER_LOC);
		assignNewEpidNumberButton.setVisible(false);
		
		TextField epidField = addField(CaseDataDto.EPID_NUMBER, TextField.class);
		epidField.setInvalidCommitted(true);
		CssStyles.style(epidField, CssStyles.ERROR_COLOR_PRIMARY);
		
		assignNewEpidNumberButton.addClickListener(e -> {
			epidField.setValue(FacadeProvider.getCaseFacade().generateEpidNumber(getValue().toReference()));
		});

		Label epidNumberWarningLabel = new Label(I18nProperties.getString(Strings.messageEpidNumberWarning));
		epidNumberWarningLabel.addStyleName(CssStyles.VSPACE_3);
		addField(CaseDataDto.EXTERNAL_ID, TextField.class);

		addField(CaseDataDto.CASE_CLASSIFICATION, OptionGroup.class);
		addField(CaseDataDto.INVESTIGATION_STATUS, OptionGroup.class);
		addField(CaseDataDto.OUTCOME, OptionGroup.class);
		addField(CaseDataDto.SEQUELAE, OptionGroup.class);
		addFields(CaseDataDto.INVESTIGATED_DATE, CaseDataDto.OUTCOME_DATE, CaseDataDto.SEQUELAE_DETAILS);

		ComboBox diseaseField = addDiseaseField(CaseDataDto.DISEASE, false);
		addField(CaseDataDto.DISEASE_DETAILS, TextField.class);
		addField(CaseDataDto.PLAGUE_TYPE, OptionGroup.class);
		addField(CaseDataDto.DENGUE_FEVER_TYPE, OptionGroup.class);
		addField(CaseDataDto.RABIES_TYPE, OptionGroup.class);

		addField(CaseDataDto.CASE_ORIGIN, TextField.class);
		
		ComboBox region = addInfrastructureField(CaseDataDto.REGION);
		ComboBox district = addInfrastructureField(CaseDataDto.DISTRICT);
		ComboBox community = addInfrastructureField(CaseDataDto.COMMUNITY);
		community.setNullSelectionAllowed(true);
		community.addStyleName(CssStyles.SOFT_REQUIRED);
		ComboBox facility = addInfrastructureField(CaseDataDto.HEALTH_FACILITY);
		facility.setImmediate(true);
		TextField facilityDetails = addField(CaseDataDto.HEALTH_FACILITY_DETAILS, TextField.class);
		facilityDetails.setVisible(false);

		region.addValueChangeListener(e -> {
			RegionReferenceDto regionDto = (RegionReferenceDto)e.getProperty().getValue();
			FieldHelper.updateItems(district, regionDto != null ? FacadeProvider.getDistrictFacade().getAllActiveByRegion(regionDto.getUuid()) : null);
		});
		district.addValueChangeListener(e -> {
			if (community.getValue() == null) {
				FieldHelper.removeItems(facility);
			}
			FieldHelper.removeItems(community);
			DistrictReferenceDto districtDto = (DistrictReferenceDto)e.getProperty().getValue();
			FieldHelper.updateItems(community, districtDto != null ? FacadeProvider.getCommunityFacade().getAllActiveByDistrict(districtDto.getUuid()) : null);
			FieldHelper.updateItems(facility, districtDto != null ? FacadeProvider.getFacilityFacade().getActiveHealthFacilitiesByDistrict(districtDto, true) : null);
		});
		community.addValueChangeListener(e -> {
			FieldHelper.removeItems(facility);
			CommunityReferenceDto communityDto = (CommunityReferenceDto)e.getProperty().getValue();
			FieldHelper.updateItems(facility, communityDto != null ? FacadeProvider.getFacilityFacade().getActiveHealthFacilitiesByCommunity(communityDto, true) :
				district.getValue() != null ? FacadeProvider.getFacilityFacade().getActiveHealthFacilitiesByDistrict((DistrictReferenceDto) district.getValue(), true) :
					null);
		});
		region.addItems(FacadeProvider.getRegionFacade().getAllActiveAsReference());
		facility.addValueChangeListener(e -> {
			updateFacilityFields(facility, facilityDetails);
		});
		
		ComboBox surveillanceOfficerField = addField(CaseDataDto.SURVEILLANCE_OFFICER, ComboBox.class);
		surveillanceOfficerField.setNullSelectionAllowed(true);
		addInfrastructureField(CaseDataDto.POINT_OF_ENTRY);
		addField(CaseDataDto.POINT_OF_ENTRY_DETAILS, TextField.class);

		addField(CaseDataDto.REPORT_LAT, TextField.class).setConverter(new StringToAngularLocationConverter());
		addField(CaseDataDto.REPORT_LON, TextField.class).setConverter(new StringToAngularLocationConverter());
		addField(CaseDataDto.REPORT_LAT_LON_ACCURACY, TextField.class);

		addField(CaseDataDto.ADDITIONAL_DETAILS, TextArea.class).setRows(3);

		addFields(CaseDataDto.PREGNANT, CaseDataDto.VACCINATION, CaseDataDto.VACCINATION_DOSES,
				CaseDataDto.VACCINATION_INFO_SOURCE, CaseDataDto.VACCINE, CaseDataDto.SMALLPOX_VACCINATION_SCAR,
				CaseDataDto.SMALLPOX_VACCINATION_RECEIVED, CaseDataDto.VACCINATION_DATE);

		// Set initial visibilities

		initializeVisibilitiesAndAllowedVisibilities(disease, viewMode);

		// Set requirements that don't need visibility changes and read only status

		setRequired(true, CaseDataDto.REPORT_DATE, CaseDataDto.CASE_CLASSIFICATION, CaseDataDto.INVESTIGATION_STATUS, CaseDataDto.OUTCOME, CaseDataDto.DISEASE, CaseDataDto.REGION, CaseDataDto.DISTRICT);
		setSoftRequired(true, CaseDataDto.INVESTIGATED_DATE, CaseDataDto.OUTCOME_DATE, CaseDataDto.PLAGUE_TYPE, CaseDataDto.SURVEILLANCE_OFFICER);
		FieldHelper.setReadOnlyWhen(getFieldGroup(), CaseDataDto.INVESTIGATED_DATE, CaseDataDto.INVESTIGATION_STATUS, Arrays.asList(InvestigationStatus.PENDING), false);
		setReadOnly(true, CaseDataDto.UUID, CaseDataDto.REPORTING_USER, CaseDataDto.CLASSIFICATION_USER, CaseDataDto.CLASSIFICATION_DATE, CaseDataDto.POINT_OF_ENTRY, 
				CaseDataDto.POINT_OF_ENTRY_DETAILS, CaseDataDto.CASE_ORIGIN);
		setReadOnly(!UserProvider.getCurrent().hasUserRight(UserRight.CASE_CHANGE_DISEASE), CaseDataDto.DISEASE);
		setReadOnly(!UserProvider.getCurrent().hasUserRight(UserRight.CASE_INVESTIGATE), CaseDataDto.INVESTIGATION_STATUS, CaseDataDto.INVESTIGATED_DATE);
		setReadOnly(!UserProvider.getCurrent().hasUserRight(UserRight.CASE_CLASSIFY), CaseDataDto.CASE_CLASSIFICATION, CaseDataDto.OUTCOME, CaseDataDto.OUTCOME_DATE);
		setReadOnly(!UserProvider.getCurrent().hasUserRight(UserRight.CASE_TRANSFER), CaseDataDto.REGION, CaseDataDto.DISTRICT, CaseDataDto.COMMUNITY, CaseDataDto.HEALTH_FACILITY, CaseDataDto.HEALTH_FACILITY_DETAILS);

		// Set conditional visibilities - ALWAYS call isVisibleAllowed before
		// dynamically setting the visibility

		if (isVisibleAllowed(CaseDataDto.PREGNANT)) {
			setVisible(person.getSex() == Sex.FEMALE, CaseDataDto.PREGNANT);
		}
		if (isVisibleAllowed(CaseDataDto.VACCINATION_DOSES)) {
			FieldHelper.setVisibleWhen(getFieldGroup(), CaseDataDto.VACCINATION_DOSES, CaseDataDto.VACCINATION,
					Arrays.asList(Vaccination.VACCINATED), true);
		}
		if (isVisibleAllowed(CaseDataDto.VACCINATION_INFO_SOURCE)) {
			FieldHelper.setVisibleWhen(getFieldGroup(), CaseDataDto.VACCINATION_INFO_SOURCE, CaseDataDto.VACCINATION,
					Arrays.asList(Vaccination.VACCINATED), true);
		}
		if (isVisibleAllowed(CaseDataDto.DISEASE_DETAILS)) {
			FieldHelper.setVisibleWhen(getFieldGroup(), Arrays.asList(CaseDataDto.DISEASE_DETAILS), CaseDataDto.DISEASE,
					Arrays.asList(Disease.OTHER), true);
			FieldHelper.setRequiredWhen(getFieldGroup(), CaseDataDto.DISEASE,
					Arrays.asList(CaseDataDto.DISEASE_DETAILS), Arrays.asList(Disease.OTHER));
		}
		if (isVisibleAllowed(CaseDataDto.PLAGUE_TYPE)) {
			FieldHelper.setVisibleWhen(getFieldGroup(), Arrays.asList(CaseDataDto.PLAGUE_TYPE), CaseDataDto.DISEASE,
					Arrays.asList(Disease.PLAGUE), true);
		}
		if (isVisibleAllowed(CaseDataDto.DENGUE_FEVER_TYPE)) {
			FieldHelper.setVisibleWhen(getFieldGroup(), Arrays.asList(CaseDataDto.DENGUE_FEVER_TYPE),
					CaseDataDto.DISEASE, Arrays.asList(Disease.DENGUE), true);
		}
		if (isVisibleAllowed(CaseDataDto.RABIES_TYPE)) {
			FieldHelper.setVisibleWhen(getFieldGroup(), Arrays.asList(CaseDataDto.RABIES_TYPE), CaseDataDto.DISEASE,
					Arrays.asList(Disease.RABIES), true);
		}
		if (isVisibleAllowed(CaseDataDto.SMALLPOX_VACCINATION_SCAR)) {
			FieldHelper.setVisibleWhen(getFieldGroup(), CaseDataDto.SMALLPOX_VACCINATION_SCAR,
					CaseDataDto.SMALLPOX_VACCINATION_RECEIVED, Arrays.asList(YesNoUnknown.YES), true);
		}
		if (isVisibleAllowed(CaseDataDto.VACCINATION_DATE)) {
			FieldHelper.setVisibleWhen(getFieldGroup(), CaseDataDto.VACCINATION_DATE,
					CaseDataDto.SMALLPOX_VACCINATION_RECEIVED, Arrays.asList(YesNoUnknown.YES), true);
			FieldHelper.setVisibleWhen(getFieldGroup(), CaseDataDto.VACCINATION_DATE, CaseDataDto.VACCINATION,
					Arrays.asList(Vaccination.VACCINATED), true);
		}
		if (isVisibleAllowed(CaseDataDto.VACCINE)) {
			FieldHelper.setVisibleWhen(getFieldGroup(), CaseDataDto.VACCINE, CaseDataDto.VACCINATION,
					Arrays.asList(Vaccination.VACCINATED), true);
		}
		if (isVisibleAllowed(CaseDataDto.OUTCOME_DATE)) {
			FieldHelper.setVisibleWhen(getFieldGroup(), CaseDataDto.OUTCOME_DATE, CaseDataDto.OUTCOME,
					Arrays.asList(CaseOutcome.DECEASED, CaseOutcome.RECOVERED), true);
		}
		if (isVisibleAllowed(CaseDataDto.SEQUELAE)) {
			FieldHelper.setVisibleWhen(getFieldGroup(), CaseDataDto.SEQUELAE,
					CaseDataDto.OUTCOME, Arrays.asList(CaseOutcome.RECOVERED, CaseOutcome.UNKNOWN), true);
		}
		if (isVisibleAllowed(CaseDataDto.SEQUELAE_DETAILS)) {
			FieldHelper.setVisibleWhen(getFieldGroup(), CaseDataDto.SEQUELAE_DETAILS,
					CaseDataDto.SEQUELAE, Arrays.asList(YesNoUnknown.YES), true);
		}
		if (isVisibleAllowed(CaseDataDto.NOTIFYING_CLINIC_DETAILS)) {
			FieldHelper.setVisibleWhen(getFieldGroup(), CaseDataDto.NOTIFYING_CLINIC_DETAILS, 
					CaseDataDto.NOTIFYING_CLINIC, Arrays.asList(HospitalWardType.OTHER), true);
		}
		setVisible(UserProvider.getCurrent().hasUserRight(UserRight.CASE_MANAGEMENT_ACCESS), CaseDataDto.CLINICIAN_NAME, CaseDataDto.CLINICIAN_PHONE, CaseDataDto.CLINICIAN_EMAIL);

		// Other initializations

		if (disease == Disease.MONKEYPOX) {
			Image smallpoxVaccinationScarImg = new Image(null, new ThemeResource("img/smallpox-vaccination-scar.jpg"));
			CssStyles.style(smallpoxVaccinationScarImg, CssStyles.VSPACE_3);
			getContent().addComponent(smallpoxVaccinationScarImg, SMALLPOX_VACCINATION_SCAR_IMG);

			// Set up initial image visibility
			getContent().getComponent(SMALLPOX_VACCINATION_SCAR_IMG).setVisible(
					getFieldGroup().getField(CaseDataDto.SMALLPOX_VACCINATION_RECEIVED).getValue() == YesNoUnknown.YES);

			// Set up image visibility listener
			getFieldGroup().getField(CaseDataDto.SMALLPOX_VACCINATION_RECEIVED).addValueChangeListener(e -> {
				getContent().getComponent(SMALLPOX_VACCINATION_SCAR_IMG)
				.setVisible(e.getProperty().getValue() == YesNoUnknown.YES);
			});
		}

		List<String> medicalInformationFields = Arrays.asList(CaseDataDto.PREGNANT, CaseDataDto.VACCINATION,
				CaseDataDto.SMALLPOX_VACCINATION_RECEIVED);

		for (String medicalInformationField : medicalInformationFields) {
			if (getFieldGroup().getField(medicalInformationField).isVisible()) {
				Label medicalInformationCaptionLabel = new Label(I18nProperties.getString(Strings.headingMedicalInformation));
				medicalInformationCaptionLabel.addStyleName(CssStyles.H3);
				getContent().addComponent(medicalInformationCaptionLabel, MEDICAL_INFORMATION_LOC);
				break;
			}
		}

		Label paperFormDatesLabel = new Label(I18nProperties.getString(Strings.headingPaperFormDates));
		paperFormDatesLabel.addStyleName(CssStyles.H3);
		getContent().addComponent(paperFormDatesLabel, PAPER_FORM_DATES_LOC);

		// Automatic case classification rules button - invisible for other diseases
		DiseaseClassificationCriteriaDto diseaseClassificationCriteria = FacadeProvider.getCaseClassificationFacade()
				.getByDisease(disease);
		if (disease != Disease.OTHER && diseaseClassificationCriteria != null) {
			Button classificationRulesButton = new Button(I18nProperties.getCaption(Captions.info), VaadinIcons.INFO_CIRCLE);
			CssStyles.style(classificationRulesButton, ValoTheme.BUTTON_PRIMARY, CssStyles.FORCE_CAPTION);
			classificationRulesButton.addClickListener(e -> {
				ControllerProvider.getCaseController().openClassificationRulesPopup(diseaseClassificationCriteria);
			});

			getContent().addComponent(classificationRulesButton, CLASSIFICATION_RULES_LOC);
		}

		addValueChangeListener(e -> {
			diseaseField.addValueChangeListener(new DiseaseChangeListener(diseaseField, getValue().getDisease()));

			// Replace classification user if case has been automatically classified
			if (getValue().getClassificationDate() != null && getValue().getClassificationUser() == null) {
				getField(CaseDataDto.CLASSIFICATION_USER).setVisible(false);
				Label classifiedBySystemLabel = new Label(I18nProperties.getCaption(Captions.system));
				classifiedBySystemLabel.setCaption(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, CaseDataDto.CLASSIFIED_BY));
				getContent().addComponent(classifiedBySystemLabel, CLASSIFIED_BY_SYSTEM_LOC);
			}
	
			setEpidNumberError(epidField, assignNewEpidNumberButton, epidNumberWarningLabel,
					getValue().getEpidNumber());
			
			epidField.addValueChangeListener(f -> {
				setEpidNumberError(epidField, assignNewEpidNumberButton, epidNumberWarningLabel,
						(String) f.getProperty().getValue());
			});
			
			// Set health facility details visibility and caption
			if (getValue().getHealthFacility() != null) {
				boolean otherHealthFacility = getValue().getHealthFacility().getUuid()
						.equals(FacilityDto.OTHER_FACILITY_UUID);
				boolean noneHealthFacility = getValue().getHealthFacility().getUuid()
						.equals(FacilityDto.NONE_FACILITY_UUID);
				boolean detailsVisible = otherHealthFacility || noneHealthFacility;

				if (isVisibleAllowed(facilityDetails)) {
					facilityDetails.setVisible(detailsVisible);
				}

				if (otherHealthFacility) {
					facilityDetails.setCaption(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX,
							CaseDataDto.HEALTH_FACILITY_DETAILS));
				}
				if (noneHealthFacility) {
					facilityDetails.setCaption(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX,
							NONE_HEALTH_FACILITY_DETAILS));
				}
			} else {
				setVisible(false, CaseDataDto.CLINICIAN_NAME, CaseDataDto.CLINICIAN_PHONE, CaseDataDto.CLINICIAN_EMAIL);
			}
			
			// Set health facility/point of entry visibility based on case origin
			if (getValue().getCaseOrigin() == CaseOrigin.POINT_OF_ENTRY) {
				setVisible(true, CaseDataDto.POINT_OF_ENTRY);
				setVisible(getValue().getPointOfEntry().isOtherPointOfEntry(), CaseDataDto.POINT_OF_ENTRY_DETAILS);
				
				if (getValue().getHealthFacility() == null) {
					setVisible(false, CaseDataDto.COMMUNITY, CaseDataDto.HEALTH_FACILITY, CaseDataDto.HEALTH_FACILITY_DETAILS);
					setReadOnly(true, CaseDataDto.REGION, CaseDataDto.DISTRICT, CaseDataDto.COMMUNITY);
				}
			} else {
				setRequired(true, CaseDataDto.HEALTH_FACILITY);
				setVisible(false, CaseDataDto.POINT_OF_ENTRY, CaseDataDto.POINT_OF_ENTRY_DETAILS);
			}
			
			// Hide case origin from port health users
			if (UserRole.isPortHealthUser(UserProvider.getCurrent().getUserRoles())) {
				setVisible(false, CaseDataDto.CASE_ORIGIN);
			}
		});
	}

	private void updateFacilityFields(ComboBox cbFacility, TextField tfFacilityDetails) {
		if (cbFacility.getValue() != null) {
			boolean otherHealthFacility = ((FacilityReferenceDto) cbFacility.getValue()).getUuid().equals(FacilityDto.OTHER_FACILITY_UUID);
			boolean noneHealthFacility = ((FacilityReferenceDto) cbFacility.getValue()).getUuid().equals(FacilityDto.NONE_FACILITY_UUID);
			boolean visibleAndRequired = otherHealthFacility || noneHealthFacility;

			tfFacilityDetails.setVisible(visibleAndRequired);
			tfFacilityDetails.setRequired(visibleAndRequired);

			if (otherHealthFacility) {
				tfFacilityDetails.setCaption(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, CaseDataDto.HEALTH_FACILITY_DETAILS));
			}
			if (noneHealthFacility) {
				tfFacilityDetails.setCaption(I18nProperties.getPrefixCaption(CaseDataDto.I18N_PREFIX, NONE_HEALTH_FACILITY_DETAILS));
			}
			if (!visibleAndRequired) {
				tfFacilityDetails.clear();
			}
		} else {
			tfFacilityDetails.setVisible(false);
			tfFacilityDetails.setRequired(false);
			tfFacilityDetails.clear();
		}	
	}
	
	@Override
	protected String createHtmlLayout() {
		return HTML_LAYOUT;
	}
	
	private void setEpidNumberError(TextField epidField, Button assignNewEpidNumberButton, Label epidNumberWarningLabel,
			String fieldValue) {
		if (FacadeProvider.getCaseFacade().doesEpidNumberExist(fieldValue, getValue().getUuid(),
				getValue().getDisease())) {
			epidField.setComponentError(new UserError(I18nProperties.getValidationError(Validations.duplicateEpidNumber)));
			assignNewEpidNumberButton.setVisible(true);
			getContent().addComponent(epidNumberWarningLabel, EPID_NUMBER_WARNING_LOC);

		} else {
			epidField.setComponentError(null);
			getContent().removeComponent(epidNumberWarningLabel);
			assignNewEpidNumberButton.setVisible(
					!CaseLogic.isEpidNumberPrefix(fieldValue) && !CaseLogic.isCompleteEpidNumber(fieldValue));
		}
	}

	private static class DiseaseChangeListener implements ValueChangeListener {

		private AbstractSelect diseaseField;
		private Disease currentDisease;

		DiseaseChangeListener(AbstractSelect diseaseField, Disease currentDisease) {
			this.diseaseField = diseaseField;
			this.currentDisease = currentDisease;
		}

		@Override
		public void valueChange(Property.ValueChangeEvent e) {

			if (diseaseField.getValue() != currentDisease) {
				ConfirmationComponent confirmDiseaseChangeComponent = new ConfirmationComponent(false) {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onConfirm() {
						diseaseField.removeValueChangeListener(DiseaseChangeListener.this);
					}

					@Override
					protected void onCancel() {
						diseaseField.setValue(currentDisease);
					}
				};
				confirmDiseaseChangeComponent.getConfirmButton().setCaption(I18nProperties.getString(Strings.confirmationChangeCaseDisease));
				confirmDiseaseChangeComponent.getCancelButton().setCaption(I18nProperties.getCaption(Captions.actionCancel));
				confirmDiseaseChangeComponent.setMargin(true);

				Window popupWindow = VaadinUiUtil.showPopupWindow(confirmDiseaseChangeComponent);
				CloseListener closeListener = new CloseListener() {
					@Override
					public void windowClose(CloseEvent e) {
						diseaseField.setValue(currentDisease);
					}
				};
				popupWindow.addCloseListener(closeListener);
				confirmDiseaseChangeComponent.addDoneListener(new DoneListener() {
					public void onDone() {
						popupWindow.removeCloseListener(closeListener);
						popupWindow.close();
					}
				});
				popupWindow.setCaption(I18nProperties.getString(Strings.headingChangeCaseDisease));
			}
		}
	}
}
