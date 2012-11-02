package org.csstudio.domain.desy.epics.pvmanager;

import gov.aps.jca.Channel;
import gov.aps.jca.dbr.CTRL;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.PRECISION;
import gov.aps.jca.dbr.STS;
import gov.aps.jca.dbr.TIME;

import java.util.ArrayList;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.csstudio.domain.desy.epics.alarm.EpicsAlarm;
import org.csstudio.domain.desy.epics.alarm.EpicsAlarmSeverity;
import org.csstudio.domain.desy.epics.alarm.EpicsAlarmStatus;
import org.csstudio.domain.desy.epics.types.ControlLimits;
import org.csstudio.domain.desy.epics.types.EpicsGraphicsData;
import org.csstudio.domain.desy.epics.types.EpicsMetaData;
import org.csstudio.domain.desy.epics.types.EpicsSystemVariable;
import org.csstudio.domain.desy.epics.types.IControlLimits;
import org.csstudio.domain.desy.epics.typesupport.EpicsSystemVariableSupport;
import org.csstudio.domain.desy.system.ControlSystem;
import org.csstudio.domain.desy.time.TimeInstant;
import org.csstudio.domain.desy.types.Limits;
import org.epics.pvmanager.DataSourceTypeAdapter;
import org.epics.pvmanager.ValueCache;

import com.google.common.collect.Lists;

abstract class DesyJCATypeAdapter implements DataSourceTypeAdapter<Channel, DesyJCAMessagePayload> {

    private final Class<?> typeClass;
    private final DBRType epicsValueType;

	private final DBRType epicsMetaType;
    private final DBRType channelFieldType;
    private Boolean array;
    EpicsMetaData _desyMeta;

    /**
     * Creates a new type adapter.
     *
     * @param typeClass the java type this adapter will create
     * @param epicsValueType the epics type used for the monitor
     * @param epicsMetaType the epics type for the get at connection time; null if no metadata is needed
     * @param array true whether this will require an array type
     */
    public DesyJCATypeAdapter(final Class<?> typeClass, final DBRType epicsValueType, final DBRType epicsMetaType, final DBRType channelFieldType) {
        this.typeClass = typeClass;
        this.epicsValueType = epicsValueType;
        this.epicsMetaType = epicsMetaType;
        this.channelFieldType=channelFieldType;
        this.array =false;
    }

    public DesyJCATypeAdapter(final Class<?> typeClass, final DBRType epicsValueType,
    		final DBRType epicsMetaType, final DBRType channelFieldType, final Boolean array) {
    	   this.typeClass = typeClass;
           this.epicsValueType = epicsValueType;
           this.epicsMetaType = epicsMetaType;
           this.channelFieldType=channelFieldType;
           this.array =array;

	}

	@Override
    public int match(final ValueCache<?> cache, final Channel channel) {

        // If the generated type can't be put in the cache, no match
        if (!cache.getType().isAssignableFrom(typeClass)) {
			return 0;
		}

        // If the type of the channel does not match, no match
        if (!dbrTypeMatch(epicsValueType, channel.getFieldType())) {
			return 0;
		}

        // If processes array, but count is 1, no match
        if (array != null &&array && channel.getElementCount() == 1) {
			return 0;
		}

        // If processes scalar, but the count is not 1, no match
        if (array != null && !array && channel.getElementCount() != 1) {
			return 0;
		}

        // Everything matches
        return 1;
    }

    private static boolean dbrTypeMatch(final DBRType aType, final DBRType anotherType) {
        return aType.isBYTE() && anotherType.isBYTE() ||
                aType.isDOUBLE() && anotherType.isDOUBLE() ||
                aType.isENUM() && anotherType.isENUM() ||
                aType.isFLOAT() && anotherType.isFLOAT() ||
                aType.isINT() && anotherType.isINT() ||
                aType.isSHORT() && anotherType.isSHORT() ||
                aType.isSTRING() && anotherType.isSTRING();
    }

    @Override
    public Object getSubscriptionParameter(final ValueCache cache, final Channel channel) {
        throw new UnsupportedOperationException("Not implemented: JCAChannelHandler is multiplexed, will not use this method");
    }

    @Override
    public boolean updateCache(final ValueCache cache, final Channel channel, final DesyJCAMessagePayload message) {
        // If metadata is required and not present, no update
        if (epicsMetaType != null && message.getMetadata() == null) {
			return false;
		}
        handleFirstCacheUpdate(message.getMetadata());
        // If value is not present, no update
        if (message.getEvent() == null) {
			return false;
		}

     //   final Object value = createValue(message.getEvent().getDBR(), message.getMetadata(), !DesyJCAChannelHandler.isChannelConnected(channel));
        final Object value = createValue(channel.getName(), message.getEvent().getDBR(), message.getMetadata(),_desyMeta);

        cache.setValue(value);
        return true;
    }
    @SuppressWarnings("rawtypes")
    private void handleFirstCacheUpdate(final DBR metadata) {
        if (_desyMeta == null) {
            _desyMeta = EpicsMetaData.EMPTY_DATA;
            if (metadata != null) {
                _desyMeta = createMetaData((STS) metadata);
            }
        }
    }
    /**
     * Creates DESY specific system variable from JCA layer, incl. time stamp conversion from
     * epoch 1990-01-01 to 1970-01-01.
     * @param channelName
     * @param eVal
     * @param eMeta
     * @param dMeta
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Nonnull
    public <V,EV extends DBR & TIME,EM extends DBR & STS> EpicsSystemVariable<V> createValue(@Nonnull final String channelName,
                                              @Nonnull final DBR eVal,
                                              @Nonnull final DBR eMeta,
                                              @Nonnull final EpicsMetaData dMeta) {

        final TimeInstant timestamp = EpicsSystemVariableSupport.toTimeInstant((TIME)eVal);

        final Object data = toData(eVal, eMeta);

        return new EpicsSystemVariable(channelName, data, ControlSystem.EPICS_DEFAULT, timestamp, dMeta);
    }


    @Nonnull
    public <W extends Comparable<? super W>>
    EpicsMetaData createMetaData(@Nonnull final STS eMeta) {
        final EpicsAlarm alarm = new EpicsAlarm(EpicsAlarmSeverity.valueOf(eMeta.getSeverity()),
                                                EpicsAlarmStatus.valueOf(eMeta.getStatus()));
        Short prec = null;
        if (eMeta instanceof PRECISION) {
            prec = Short.valueOf(((PRECISION) eMeta).getPrecision());
        }
        EpicsGraphicsData<W> gr = null;
        IControlLimits<W> cr = null;
        if (eMeta instanceof CTRL) {
            final CTRL ctrl = (CTRL) eMeta;
            gr = createGraphics(ctrl);
            cr = createControlLimits(ctrl);
        }
        return EpicsMetaData.create(alarm, gr, cr, prec);
    }

    @SuppressWarnings("unchecked")
	@Nonnull
    protected <W extends Comparable<? super W>>
    EpicsGraphicsData<W> createGraphics(@Nonnull final CTRL ctrl) {
        final Limits<W> aLimits = Limits.create((W) ctrl.getLowerAlarmLimit(), (W) ctrl.getUpperAlarmLimit());
        final Limits<W> wLimits = Limits.create((W) ctrl.getLowerWarningLimit(), (W) ctrl.getUpperWarningLimit());
        final Limits<W> oLimits = Limits.create((W) ctrl.getLowerDispLimit(), (W) ctrl.getUpperDispLimit());
        return new EpicsGraphicsData<W>(aLimits, wLimits, oLimits);
    }

    @SuppressWarnings("unchecked")
	@Nonnull
    protected <W extends Comparable<? super W>>
    IControlLimits<W> createControlLimits(@Nonnull final CTRL ctrl) {
        return new ControlLimits<W>((W) ctrl.getLowerCtrlLimit(), (W) ctrl.getUpperCtrlLimit());
    }
    /**
     * Given the value and the (optional) metadata, will create the new value.
     *
     * @param value the value taken from the monitor
     * @param metadata the value taken as metadata
     * @param disconnected true if the value should report the channel is currently disconnected
     * @return the new value
     */
    public Object createValue(final DBR value, final DBR metadata, final boolean disconnected){
    return null;
    }
    @Nonnull
    public Object toData(@Nonnull final DBR eVal, @Nonnull final Object eMeta) {
        final int nelm = eVal.getCount();
         if(nelm>1){
        final ArrayList array = Lists.newArrayListWithCapacity(nelm);

        for (int i = 0; i < nelm; i++) {
            array.add(toData(eVal, eMeta, i));
        }
        this.array=true;
        return array;
        }else{
        	this.array=false;
        	return toData(eVal, eMeta, 0);
        }
    }
    @Nonnull
    abstract Object toData(@Nonnull final DBR eVal,
                                   @CheckForNull final Object eMeta,
                                   final int index);
    public boolean isArray() {
        return array;
    }
    /**
   	 * @return the typeClass
   	 */
   	public Class<?> getTypeClass() {
   		return typeClass;
   	}

   	/**
   	 * @return the epicsValueType
   	 */
   	public DBRType getEpicsValueType() {
   		return epicsValueType;
   	}

   	/**
   	 * @return the epicsMetaType
   	 */
   	public DBRType getEpicsMetaType() {
   		return epicsMetaType;
   	}

   	/**
   	 * @return the channelFieldType
   	 */
   	public DBRType getChannelFieldType() {
   		return channelFieldType;
   	}
}
