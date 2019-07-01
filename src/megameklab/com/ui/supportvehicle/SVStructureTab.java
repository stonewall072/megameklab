/*
 * MegaMekLab
 * Copyright (C) 2019 The MegaMek Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package megameklab.com.ui.supportvehicle;

import megamek.common.*;
import megamek.common.verifier.TestEntity;
import megamek.common.verifier.TestSupportVehicle;
import megameklab.com.MegaMekLab;
import megameklab.com.ui.EntitySource;
import megameklab.com.ui.view.*;
import megameklab.com.ui.view.listeners.SVBuildListener;
import megameklab.com.util.ITab;
import megameklab.com.util.RefreshListener;
import megameklab.com.util.UnitUtil;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Structure tab for support vehicle construction
 */
class SVStructureTab extends ITab implements SVBuildListener {

    private RefreshListener refresh = null;
    private JPanel masterPanel;
    private BasicInfoView panBasicInfo;
    private SVChassisView panChassis;
    private MovementView panMovement;
    private FuelView panFuel;
    private SVSummaryView panSummary;
    private ChassisModView panChassisMod;
    private SVCrewView panCrew;

    SVStructureTab(EntitySource eSource) {
        super(eSource);
        setLayout(new BorderLayout());
        setupPanels();
        add(masterPanel, BorderLayout.CENTER);
        refresh();
    }

    private Entity getSV() {
        return eSource.getEntity();
    }

    private void setupPanels() {
        masterPanel = new JPanel(new GridBagLayout());
        panBasicInfo = new BasicInfoView(getSV().getConstructionTechAdvancement());
        panChassis = new SVChassisView(panBasicInfo);
        panMovement = new MovementView(panBasicInfo);
        panFuel = new FuelView();
        panSummary = new SVSummaryView(eSource);
        panChassisMod = new ChassisModView(panBasicInfo);
        panCrew = new SVCrewView();

        JPanel leftPanel = new JPanel();
        JPanel midPanel = new JPanel();
        JPanel rightPanel = new JPanel();
        leftPanel.setLayout(new GridBagLayout());
        midPanel.setLayout(new GridBagLayout());
        rightPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 0;
        leftPanel.add(panBasicInfo, gbc);
        gbc.gridy++;
        leftPanel.add(panChassis, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        midPanel.add(panMovement, gbc);
        gbc.gridy++;
        midPanel.add(panFuel, gbc);
        gbc.gridy++;
        midPanel.add(panSummary, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        rightPanel.add(panChassisMod, gbc);
        gbc.gridy++;
        rightPanel.add(panCrew, gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = java.awt.GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        masterPanel.add(leftPanel, gbc);
        gbc.gridx = 1;
        masterPanel.add(midPanel, gbc);
        gbc.gridx = 2;
        masterPanel.add(rightPanel, gbc);

        panBasicInfo.setBorder(BorderFactory.createTitledBorder("Basic Information"));
        panChassis.setBorder(BorderFactory.createTitledBorder("Chassis"));
        panMovement.setBorder(BorderFactory.createTitledBorder("Movement"));
        panFuel.setBorder(BorderFactory.createTitledBorder("Fuel"));
        panSummary.setBorder(BorderFactory.createTitledBorder("Summary"));
        panChassisMod.setBorder(BorderFactory.createTitledBorder("Chassis Modifications"));
        panCrew.setBorder(BorderFactory.createTitledBorder("Crew and Quarters"));
    }

    public void refresh() {
        removeAllListeners();

        panBasicInfo.setFromEntity(getSV());
        panChassis.setFromEntity(getSV());
        panMovement.setFromEntity(getSV());
        refreshFuel();
        panChassisMod.setFromEntity(getSV());
        panCrew.setFromEntity(getSV());
        panSummary.refresh();

        addAllListeners();
    }

    public ITechManager getTechManager() {
        return panBasicInfo;
    }

    private void removeAllListeners() {
        panBasicInfo.removeListener(this);
        panChassis.removeListener(this);
        panMovement.removeListener(this);
        panFuel.removeListener(this);
        panChassisMod.removeListener(this);
        panCrew.removeListener(this);
    }

    private void addAllListeners() {
        panBasicInfo.addListener(this);
        panChassis.addListener(this);
        panMovement.addListener(this);
        panFuel.addListener(this);
        panChassisMod.addListener(this);
        panCrew.addListener(this);
    }

    public void addRefreshedListener(RefreshListener l) {
        refresh = l;
    }

    @Override
    public void refreshSummary() {
        panSummary.refresh();
    }

    @Override
    public void chassisChanged(String chassis) {
        getSV().setChassis(chassis);
        refresh.refreshHeader();
        refresh.refreshPreview();
    }

    @Override
    public void modelChanged(String model) {
        getSV().setModel(model);
        refresh.refreshHeader();
        refresh.refreshPreview();
    }

    @Override
    public void yearChanged(int year) {
        getSV().setYear(year);
        updateTechLevel();
    }

    @Override
    public void updateTechLevel() {
        if (UnitUtil.checkEquipmentByTechLevel(getSV(), panBasicInfo)) {
            refresh.refreshEquipment();
        } else {
            refresh.refreshEquipmentTable();
        }
        panChassis.refresh();
        panMovement.setFromEntity(getSV());
        panChassisMod.setFromEntity(getSV());
        refresh.refreshArmor();
    }

    @Override
    public void sourceChanged(String source) {
        getSV().setSource(source);
        refresh.refreshPreview();
    }

    @Override
    public void techBaseChanged(boolean clan, boolean mixed) {
        if ((clan != getSV().isClan()) || (mixed != getSV().isMixedTech())) {
            getSV().setMixedTech(mixed);
            updateTechLevel();
        }
    }

    @Override
    public void techLevelChanged(SimpleTechLevel techLevel) {
        updateTechLevel();
    }

    @Override
    public void manualBVChanged(int manualBV) {
        getSV().setManualBV(manualBV);
    }

    @Override
    public void walkChanged(int walkMP) {
        getSV().setOriginalWalkMP(walkMP);
        panSummary.refresh();
        refresh.refreshStatus();
        refresh.refreshPreview();
        panMovement.removeListener(this);
        panMovement.setFromEntity(getSV());
        panMovement.addListener(this);
        panFuel.setFromEntity(getSV());
        panChassis.refresh();
    }

    @Override
    public void jumpChanged(int jumpMP, EquipmentType jumpJet) {
        if (null != jumpJet) {
            UnitUtil.removeAllMiscMounteds(getSV(), MiscType.F_JUMP_JET);
            getSV().setOriginalJumpMP(0);
            for (int i = 0; i < jumpMP; i++) {
                try {
                    getSV().addEquipment(jumpJet, Tank.LOC_BODY);
                } catch (LocationFullException e) {
                    e.printStackTrace();
                }
            }
            panSummary.refresh();
            refresh.refreshBuild();
            refresh.refreshStatus();
            refresh.refreshPreview();
            panMovement.removeListener(this);
            panMovement.setFromEntity(getSV());
            panMovement.addListener(this);
        }
    }

    @Override
    public void jumpTypeChanged(EquipmentType jumpJet) {
        // Only one type of JJ for vehicles
    }

    @Override
    public void tonnageChanged(double tonnage) {
        getSV().setWeight(TestEntity.ceil(tonnage, tonnage < 5 ?
                TestEntity.Ceil.KILO : TestEntity.Ceil.HALFTON));
        panChassisMod.setFromEntity(getSV());
        panFuel.setFromEntity(getSV());
        refresh.refreshArmor();
        refresh.refreshEquipmentTable();
        refresh.refreshSummary();
        refresh.refreshStatus();
        refresh.refreshPreview();
    }

    @Override
    public void typeChanged(TestSupportVehicle.SVType type) {
        TestSupportVehicle.SVType oldType = TestSupportVehicle.SVType.getVehicleType(getSV());
        if (!oldType.equals(type)) {
            if (type.equals(TestSupportVehicle.SVType.FIXED_WING)) {
                eSource.createNewUnit(Entity.ETYPE_FIXED_WING_SUPPORT, getSV());
            } else if (type.equals(TestSupportVehicle.SVType.VTOL)) {
                eSource.createNewUnit(Entity.ETYPE_SUPPORT_VTOL, getSV());
            } else if (oldType.equals(TestSupportVehicle.SVType.FIXED_WING)
                    || oldType.equals(TestSupportVehicle.SVType.VTOL)) {
                eSource.createNewUnit(Entity.ETYPE_SUPPORT_TANK, getSV());
            }
            getSV().setMovementMode(type.defaultMovementMode);
            panChassis.setFromEntity(getSV());
            panMovement.setFromEntity(getSV());
            refreshFuel();
            panChassisMod.setFromEntity(getSV());
            panCrew.setFromEntity(getSV());
            panSummary.refresh();
            refresh.refreshEquipmentTable();
            refresh.refreshBuild();
            refresh.refreshStatus();
            refresh.refreshPreview();
            //TODO: Refresh other views
        }
    }

    @Override
    public void structuralTechRatingChanged(int techRating) {
        getSV().setStructuralTechRating(techRating);
        panSummary.refresh();
        refresh.refreshStatus();
        refresh.refreshPreview();
    }

    @Override
    public void engineChanged(Engine engine) {
        getSV().setEngine(engine);
        // Make sure the engine tech rating is at least the minimum for the engine type
        if (getSV().getEngineTechRating() < engine.getTechRating()) {
            getSV().setEngineTechRating(engine.getTechRating());
        }
        // Fixed Wing support vehicles require the prop mod for an electric engine
        if ((TestSupportVehicle.SVType.getVehicleType(getSV()) == TestSupportVehicle.SVType.FIXED_WING)
                && TestSupportVehicle.SVEngine.getEngineType(engine).electric
                && !getSV().hasMisc(MiscType.F_PROP)) {
            setChassisMod(TestSupportVehicle.ChassisModification.PROP.equipment, true);
        }
        // MagLev trains cannot use the external power pickup mod.
        if (engine.getEngineType() == Engine.MAGLEV) {
            setChassisMod(TestSupportVehicle.ChassisModification.EXTERNAL_POWER_PICKUP.equipment, false);
        }
        // The chassis view needs to refresh the available engine rating combobox
        panChassis.removeListener(this);
        panChassis.setFromEntity(getSV());
        panChassis.addListener(this);
        refreshFuel();
        panChassisMod.setFromEntity(getSV());
        panSummary.refresh();
        refresh.refreshStatus();
        refresh.refreshPreview();
    }

    @Override
    public void engineTechRatingChanged(int techRating) {
        getSV().setEngineTechRating(techRating);
        panFuel.setFromEntity(getSV());
        panSummary.refresh();
        refresh.refreshStatus();
        refresh.refreshPreview();
    }

    @Override
    public void setChassisMod(EquipmentType mod, boolean installed) {
        final Mounted current = getSV().getMisc().stream().filter(m -> m.getType().equals(mod)).findFirst().orElse(null);
        if (installed && (null == current)) {
            try {
                getSV().addEquipment(mod, getSV().isAero() ? FixedWingSupport.LOC_BODY : Tank.LOC_BODY);
            } catch (LocationFullException e) {
                // This should not be possible since chassis mods don't occupy slots
                MegaMekLab.getLogger().error(getClass(), "setChassisMod(EquipmentType, boolean)",
                        "LocationFullException when adding chassis mod " + mod.getName());
            }
        } else if (!installed && (null != current)) {
            getSV().getMisc().remove(current);
            getSV().getEquipment().remove(current);
            UnitUtil.removeCriticals(getSV(), current);
        }
        if (mod.equals(TestSupportVehicle.ChassisModification.OMNI.equipment)) {
            getSV().setOmni(installed);
            panChassis.setFromEntity(getSV());
        } else if (mod.equals(TestSupportVehicle.ChassisModification.ARMORED.equipment)) {
            refresh.refreshArmor();
        }
        refreshFuel();
        panChassisMod.refresh();
        panSummary.refresh();
        refresh.refreshStatus();
        refresh.refreshPreview();
    }

    @Override
    public void turretChanged(int turretConfig) {
        if (!(getSV() instanceof Tank)) {
            return;
        }
        if ((turretConfig != SVBuildListener.TURRET_DUAL)
                && !getTank().hasNoDualTurret()) {
            removeTurret(getTank().getLocTurret2());
            getTank().setHasNoDualTurret(true);
            getTank().setBaseChassisTurret2Weight(-1);
        }
        if ((turretConfig == SVBuildListener.TURRET_NONE)
                && !getTank().hasNoTurret()) {
            removeTurret(getTank().getLocTurret());
            getTank().setHasNoTurret(true);
            getTank().setBaseChassisTurretWeight(-1);
        }

        if (getTank().hasNoTurret() && (turretConfig != SVBuildListener.TURRET_NONE)) {
            getTank().setHasNoTurret(false);
            getTank().autoSetInternal();
            initTurretArmor(getTank().getLocTurret());
        }
        if (getTank().hasNoDualTurret() && (turretConfig == SVBuildListener.TURRET_DUAL)) {
            getTank().setHasNoDualTurret(false);
            getTank().autoSetInternal();
            initTurretArmor(getTank().getLocTurret2());
        }
        getTank().autoSetInternal();
        panChassis.setFromEntity(getTank());
        //TODO: Refresh armor tab
        refresh.refreshBuild();
        refresh.refreshPreview();
        refresh.refreshStatus();
    }

    @Override
    public void turretBaseWtChanged(double turret1, double turret2) {
        if (getSV() instanceof Tank) {
            getTank().setBaseChassisTurretWeight(turret1);
            getTank().setBaseChassisTurret2Weight(turret2);
            panSummary.refresh();
            refresh.refreshStatus();
        }
    }

    @Override
    public void fireConChanged(int index) {
        final Mounted current = getSV().getMisc().stream()
                .filter(m -> m.getType().hasFlag(MiscType.F_BASIC_FIRECONTROL)
                        || m.getType().hasFlag(MiscType.F_ADVANCED_FIRECONTROL))
                .findFirst().orElse(null);
        if (null != current) {
            getSV().getMisc().remove(current);
            getSV().getEquipment().remove(current);
            UnitUtil.removeCriticals(getSV(), current);
        }
        EquipmentType eq = null;
        if (index == SVBuildListener.FIRECON_BASIC) {
            eq = EquipmentType.get("Basic Fire Control"); //$NON-NLS-1$
        } else if (index == SVBuildListener.FIRECON_ADVANCED) {
            eq = EquipmentType.get("Advanced Fire Control"); //$NON-NLS-1$
        }
        if (null != eq) {
            try {
                getSV().addEquipment(eq, getSV().isAero() ? FixedWingSupport.LOC_BODY : Tank.LOC_BODY);
            } catch (LocationFullException e) {
                // This should not be possible since fire control doesn't occupy slots
                MegaMekLab.getLogger().error(getClass(), "fireConChanged(int)",
                        "LocationFullException when adding fire control " + eq.getName());
            }
        }
        panChassis.setFromEntity(getSV());
        panSummary.refresh();
        refresh.refreshStatus();
        refresh.refreshPreview();
    }

    @Override
    public void fireConWtChanged(double weight) {
        getSV().setBaseChassisFireConWeight(weight);
        panSummary.refresh();
        refresh.refreshStatus();
    }

    @Override
    public void setSeating(int standard, int pillion, int ejection) {
        // Clear out any existing seating.
        final List<Transporter> current = getSV().getTransports().stream()
                .filter(t -> t instanceof StandardSeatCargoBay)
                .collect(Collectors.toList());
        for (Transporter t : current) {
            getSV().removeTransporter(t);
        }
        // Create new ones as needed.
        if (standard > 0) {
            getSV().addTransporter(new StandardSeatCargoBay(standard));
        }
        if (pillion > 0) {
            getSV().addTransporter(new PillionSeatCargoBay(pillion));
        }
        if (ejection > 0) {
            getSV().addTransporter(new EjectionSeatCargoBay(ejection));
        }
        panCrew.setFromEntity(getSV());
        panSummary.refresh();
        refresh.refreshStatus();
        refresh.refreshPreview();
    }

    @Override
    public void setQuarters(int firstClass, int secondClass, int crew, int steerage) {
        // Clear out any existing standard or pillion seating.
        final List<Transporter> current = getSV().getTransports().stream()
                .filter(t -> (t instanceof FirstClassQuartersCargoBay)
                    || (t instanceof SecondClassQuartersCargoBay)
                    || (t instanceof CrewQuartersCargoBay)
                    || (t instanceof SteerageQuartersCargoBay))
                .collect(Collectors.toList());
        for (Transporter t : current) {
            getSV().removeTransporter(t);
        }
        // Create new ones as needed.
        if (firstClass > 0) {
            getSV().addTransporter(new FirstClassQuartersCargoBay(firstClass));
        }
        if (secondClass > 0) {
            getSV().addTransporter(new SecondClassQuartersCargoBay(secondClass));
        }
        if (crew > 0) {
            getSV().addTransporter(new CrewQuartersCargoBay(crew));
        }
        if (steerage > 0) {
            getSV().addTransporter(new SteerageQuartersCargoBay(steerage));
        }
        panCrew.setFromEntity(getSV());
        panSummary.refresh();
        refresh.refreshStatus();
        refresh.refreshPreview();
    }

    private void removeTurret(int loc) {
        for (int slot = 0; slot < getTank().getNumberOfCriticals(loc); slot++) {
            getTank().setCritical(loc, slot, null);
        }
        for (Mounted mount : getTank().getEquipment()) {
            if (mount.getLocation() == loc) {
                UnitUtil.changeMountStatus(getTank(), mount,
                        Entity.LOC_NONE, Entity.LOC_NONE, false);
            }
        }
    }

    private void initTurretArmor(int loc) {
        getTank().initializeArmor(0, loc);
        getTank().setArmorTechLevel(
                getTank().getArmorTechLevel(Tank.LOC_FRONT),
                loc);
        getTank().setArmorType(getTank().getArmorType(Tank.LOC_FRONT),
                loc);
    }

    @Override
    public void fuelTonnageChanged(double tonnage) {
        double fuelTons = Math.round(tonnage * 2) / 2.0;
        if (getSV().isAero()) {
            getAero().setFuelTonnage(fuelTons);
        } else {
            getTank().setFuelTonnage(fuelTons);
        }
        refreshFuel();
        panSummary.refresh();
        refresh.refreshStatus();
        refresh.refreshPreview();
    }

    /**
     * Convenience method that removes the fuel if the vehicle does not require fuel mass
     * then refreshes the fuel panel. Changes that can affect this are vehicle type, engine
     * type, and the prop chassis mod.
     */
    private void refreshFuel() {
        if ((getSV() instanceof FixedWingSupport)
                && (((FixedWingSupport) getSV()).kgPerFuelPoint() == 0)) {
            getAero().setFuelTonnage(0);
        } else if ((getSV() instanceof Tank) && (getTank().fuelTonnagePer100km() == 0)) {
            getTank().setFuelTonnage(0);
        }
        panFuel.setFromEntity(getSV());
    }
}
