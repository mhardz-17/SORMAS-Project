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
package de.symeda.sormas.ui.configuration.infrastructure;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import de.symeda.sormas.api.I18nProperties;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.configuration.AbstractConfigurationView;
import de.symeda.sormas.ui.login.LoginHelper;
import de.symeda.sormas.ui.utils.CssStyles;

public class RegionsView extends AbstractConfigurationView {

	private static final long serialVersionUID = -3487830069266335042L;
	
	public static final String SEARCH = "search";

	public static final String VIEW_NAME = ROOT_VIEW_NAME + "/regions";
	
	private HorizontalLayout filterLayout;
	private VerticalLayout gridLayout;
	private RegionsGrid grid;	
	protected Button createButton;

	public RegionsView() {
		super(VIEW_NAME);
		grid = new RegionsGrid();
		gridLayout = new VerticalLayout();
		gridLayout.addComponent(createFilterBar());
		gridLayout.addComponent(grid);
		gridLayout.setMargin(true);
		gridLayout.setSpacing(false);
		gridLayout.setExpandRatio(grid, 1);
		gridLayout.setSizeFull();
		gridLayout.setStyleName("crud-main-layout");
		grid.reload();
		
		if (LoginHelper.hasUserRight(UserRight.INFRASTRUCTURE_CREATE)) {
			createButton = new Button("New entry");
			createButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
			createButton.setIcon(FontAwesome.PLUS_CIRCLE);
			createButton.addClickListener(
					e -> ControllerProvider.getInfrastructureController().createRegion());
			addHeaderComponent(createButton);
		}
		
		addComponent(gridLayout);
	}
	
	private HorizontalLayout createFilterBar() {
		filterLayout = new HorizontalLayout();
		filterLayout.setSpacing(true);
		filterLayout.setSizeUndefined();

		TextField searchField = new TextField();
		searchField.setWidth(200, Unit.PIXELS);
		searchField.setInputPrompt(I18nProperties.getText(SEARCH));
		searchField.addTextChangeListener(e -> {
			grid.filterByText(e.getText());
		});
		CssStyles.style(searchField, CssStyles.FORCE_CAPTION);
		filterLayout.addComponent(searchField);
		
		return filterLayout;
	}
	
}