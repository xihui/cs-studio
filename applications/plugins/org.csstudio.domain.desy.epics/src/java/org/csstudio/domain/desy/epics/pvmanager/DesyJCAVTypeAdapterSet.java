package org.csstudio.domain.desy.epics.pvmanager;

import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Byte;
import gov.aps.jca.dbr.DBR_CTRL_Byte;
import gov.aps.jca.dbr.DBR_CTRL_Double;
import gov.aps.jca.dbr.DBR_CTRL_Float;
import gov.aps.jca.dbr.DBR_CTRL_Int;
import gov.aps.jca.dbr.DBR_CTRL_Short;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_Float;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.dbr.DBR_LABELS_Enum;
import gov.aps.jca.dbr.DBR_STS_String;
import gov.aps.jca.dbr.DBR_Short;
import gov.aps.jca.dbr.DBR_String;
import gov.aps.jca.dbr.DBR_TIME_Byte;
import gov.aps.jca.dbr.DBR_TIME_Double;
import gov.aps.jca.dbr.DBR_TIME_Enum;
import gov.aps.jca.dbr.DBR_TIME_Float;
import gov.aps.jca.dbr.DBR_TIME_Int;
import gov.aps.jca.dbr.DBR_TIME_Short;
import gov.aps.jca.dbr.DBR_TIME_String;
import gov.aps.jca.dbr.LABELS;
import gov.aps.jca.dbr.STS;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.csstudio.domain.desy.epics.alarm.EpicsAlarm;
import org.csstudio.domain.desy.epics.alarm.EpicsAlarmSeverity;
import org.csstudio.domain.desy.epics.alarm.EpicsAlarmStatus;
import org.csstudio.domain.desy.epics.types.EpicsEnum;
import org.csstudio.domain.desy.epics.types.EpicsMetaData;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

public class DesyJCAVTypeAdapterSet  implements DesyJCATypeAdapterSet {

		    @Override
		    public Set<DesyJCATypeAdapter> getAdapters() {
		        return converters;
		    }
		    private static final Set<DesyJCATypeAdapter> converters;
		    static Pattern longStringPattern = Pattern.compile(".+\\..*\\$.*");

		    static {
		        final Set<DesyJCATypeAdapter> newFactories = new HashSet<DesyJCATypeAdapter>();
		        newFactories.add( new DesyJCATypeAdapter(Float.class,
                        DBR_TIME_Float.TYPE,
                        DBR_CTRL_Float.TYPE,
                        DBR_Float.TYPE,
                        false){
                    @Override
                    @Nonnull
                    Object toData(@Nonnull final DBR eVal, @CheckForNull final Object eMeta, final int index) {
                        return Float.valueOf(((DBR_TIME_Float) eVal).getFloatValue()[index]);
                    }



                });
		        newFactories.add( new DesyJCATypeAdapter(Float.class,
                        DBR_TIME_Float.TYPE,
                        DBR_CTRL_Float.TYPE,
                        DBR_Float.TYPE, true){
		        	   @Override
		        	   @Nonnull
	                    public Float toData(@Nonnull final DBR eVal, @CheckForNull final Object eMeta, final int index) {
	                        return Float.valueOf(((DBR_TIME_Float) eVal).getFloatValue()[index]);
	                    }
		        });
		        newFactories.add( new DesyJCATypeAdapter(Double.class,
                        DBR_TIME_Double.TYPE,
                        DBR_CTRL_Double.TYPE,
                        DBR_Double.TYPE,false){
		        	   @Override
                    @Nonnull
                    public Double toData(@Nonnull final DBR eVal, @CheckForNull final Object eMeta, final int index) {
                        return Double.valueOf(((DBR_TIME_Double) eVal).getDoubleValue()[index]);
                    }
                });
		        newFactories.add( new DesyJCATypeAdapter(Double.class,
                        DBR_TIME_Double.TYPE,
                        DBR_CTRL_Double.TYPE,
                        DBR_Double.TYPE,true){
		        	   @Override
                    @Nonnull
                    public Double toData(@Nonnull final DBR eVal, @CheckForNull final Object eMeta, final int index) {
                        return Double.valueOf(((DBR_TIME_Double) eVal).getDoubleValue()[index]);
                    }
                });
		        newFactories.add( new DesyJCATypeAdapter(Byte.class,
                        DBR_TIME_Byte.TYPE,
                        DBR_CTRL_Byte.TYPE,
                        DBR_Byte.TYPE,false){
		        	@Override
                    @Nonnull
                    public Byte toData(@Nonnull final DBR eVal, @CheckForNull final Object eMeta, final int index) {
                        return Byte.valueOf(((DBR_TIME_Byte) eVal).getByteValue()[index]);
                    }
                });
		        newFactories.add( new DesyJCATypeAdapter(Byte.class,
                        DBR_TIME_Byte.TYPE,
                        DBR_CTRL_Byte.TYPE,
                        DBR_Byte.TYPE,true){
		        	@Override
                    @Nonnull
                    public Byte toData(@Nonnull final DBR eVal, @CheckForNull final Object eMeta, final int index) {
                        return Byte.valueOf(((DBR_TIME_Byte) eVal).getByteValue()[index]);
                    }
                });
		        newFactories.add( new DesyJCATypeAdapter(Short.class,
                        DBR_TIME_Short.TYPE,
                        DBR_CTRL_Short.TYPE,
                        DBR_Short.TYPE){
		        	@Override
                    @Nonnull
                    public Short toData(@Nonnull final DBR eVal, @CheckForNull final Object eMeta, final int index) {
                        return Short.valueOf(((DBR_TIME_Short) eVal).getShortValue()[index]);
                    }
                });
		        newFactories.add( new DesyJCATypeAdapter(Short.class,
                        DBR_TIME_Short.TYPE,
                        DBR_CTRL_Short.TYPE,
                        DBR_Short.TYPE,true){
		        	@Override
                    @Nonnull
                    public Short toData(@Nonnull final DBR eVal, @CheckForNull final Object eMeta, final int index) {
                        return Short.valueOf(((DBR_TIME_Short) eVal).getShortValue()[index]);
                    }
                });
		        newFactories.add(  new DesyJCATypeAdapter(Integer.class,
                        DBR_TIME_Int.TYPE,
                        DBR_CTRL_Int.TYPE,
                        DBR_Int.TYPE){
		        	@Override
                    @Nonnull
                    public Integer toData(@Nonnull final DBR eVal, @CheckForNull final Object eMeta, final int index) {
                        return Integer.valueOf(((DBR_TIME_Int) eVal).getIntValue()[index]);
                    }
                });
		        newFactories.add(  new DesyJCATypeAdapter(Integer.class,
                        DBR_TIME_Int.TYPE,
                        DBR_CTRL_Int.TYPE,
                        DBR_Int.TYPE,true){
		        	@Override
                    @Nonnull
                    public Integer toData(@Nonnull final DBR eVal, @CheckForNull final Object eMeta, final int index) {
                        return Integer.valueOf(((DBR_TIME_Int) eVal).getIntValue()[index]);
                    }
                });

		        newFactories.add(  new DesyJCATypeAdapter(String.class,
                        DBR_TIME_String.TYPE,
                        DBR_STS_String.TYPE,
                        DBR_String.TYPE){
		        	@Override
                    @Nonnull
                    public String toData(@Nonnull final DBR eVal, @CheckForNull final Object eMeta, final int index) {
                        return ((DBR_TIME_String) eVal).getStringValue()[index];
                    }

                    @Override
                    @CheckForNull
                    public EpicsMetaData createMetaData(@Nonnull final STS eMeta) {
                        return EpicsMetaData.create(new EpicsAlarm(EpicsAlarmSeverity.valueOf(eMeta.getSeverity()),
                                                                   EpicsAlarmStatus.valueOf(eMeta.getStatus())),
                                                                   null, null, null);
                    }
                });
		        newFactories.add(  new DesyJCATypeAdapter(String.class,
                        DBR_TIME_String.TYPE,
                        DBR_STS_String.TYPE,
                        DBR_String.TYPE,true){
		        	@Override
                    @Nonnull
                    public String toData(@Nonnull final DBR eVal, @CheckForNull final Object eMeta, final int index) {
                        return ((DBR_TIME_String) eVal).getStringValue()[index];
                    }

                    @Override
                    @CheckForNull
                    public EpicsMetaData createMetaData(@Nonnull final STS eMeta) {
                        return EpicsMetaData.create(new EpicsAlarm(EpicsAlarmSeverity.valueOf(eMeta.getSeverity()),
                                                                   EpicsAlarmStatus.valueOf(eMeta.getStatus())),
                                                                   null, null, null);
                    }
                });
		        newFactories.add(  new DesyJCATypeAdapter(EpicsEnum.class,
                        DBR_TIME_Enum.TYPE,
                        DBR_LABELS_Enum.TYPE,
                        DBR_Enum.TYPE){
                    @Override
                    @Nonnull
                    public EpicsEnum toData(@Nonnull final DBR eVal, @CheckForNull final Object eMeta, final int index) {
                        final short i = ((DBR_TIME_Enum) eVal).getEnumValue()[index];
                        final String[] labels = eMeta!= null ? ((DBR_LABELS_Enum)eMeta).getLabels() : null;
                        if (labels != null && i >=0 && i < labels.length && !Strings.isNullOrEmpty(labels[i])) {
                            return EpicsEnum.createFromState(labels[i], (int) i);
                        }
                        return EpicsEnum.createFromRaw(Integer.valueOf(i));
                    }
                    @Override
                    @Nonnull
                    public EpicsMetaData createMetaData(@Nonnull final STS eMeta) {
                        return EpicsMetaData.create(((LABELS) eMeta).getLabels());
                    }
                });
		        newFactories.add(  new DesyJCATypeAdapter(EpicsEnum.class,
                        DBR_TIME_Enum.TYPE,
                        DBR_LABELS_Enum.TYPE,
                        DBR_Enum.TYPE,true){
		        	@Override
                    @Nonnull
                    public EpicsEnum toData(@Nonnull final DBR eVal, @CheckForNull final Object eMeta, final int index) {
                        final short i = ((DBR_TIME_Enum) eVal).getEnumValue()[index];
                        final String[] labels = eMeta!= null ? ((DBR_LABELS_Enum)eMeta).getLabels() : null;
                        if (labels != null && i >=0 && i < labels.length && !Strings.isNullOrEmpty(labels[i])) {
                            return EpicsEnum.createFromState(labels[i], (int) i);
                        }
                        return EpicsEnum.createFromRaw(Integer.valueOf(i));
                    }
                    @Override
                    @Nonnull
                    public EpicsMetaData createMetaData(@Nonnull final STS eMeta) {
                        return EpicsMetaData.create(((LABELS) eMeta).getLabels());
                    }
                });
                converters = Collections.unmodifiableSet(newFactories);
		      }

		    @Nonnull
		    public static Set<Class<?>> getInstalledTargetTypes() {
		        final Set<Class<?>> targetTypes = Sets.newHashSet();
		        for (@SuppressWarnings("rawtypes") final DesyJCATypeAdapter fac : converters) {
		            targetTypes.add(fac.getTypeClass());
		        }
		        return targetTypes;
		    }

}
