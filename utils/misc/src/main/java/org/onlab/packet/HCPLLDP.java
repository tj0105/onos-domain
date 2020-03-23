package org.onlab.packet;


import org.apache.commons.lang.ArrayUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @Author ldy
 * @Date: 20-3-3 下午3:15
 * @Version 1.0
 */
public class HCPLLDP extends LLDP {
    public static final byte[] ONLAB_OUI={(byte)0xa4,0x23,0x05};
    //devicesID prefix
    public static final String HCP_CHASSIS_VALUE_PREFIX="dpid:";
    //domainId prefix
    public static final String HCP_DOMAINID_VALUE_PREFIX="domainId:";
    //hcplldp name
    public static final String HCP_ORGANIZATION_NAME="HCP Discovery";
    //lldp name subtype
    protected static final byte NAME_SUBTYPE=1;
    //lldp device subtype
    protected static final byte DEVICE_SUBTYPE=2;
    //lldp domain subtype
    protected static final byte DOMAIN_SUBTYPE=3;

    private static final short NAME_LENGTH=LLDPOrganizationalTLV.OUI_LENGTH+LLDPOrganizationalTLV.SUBTYPE_LENGTH;
    private static final short DEVICE_LENGTH=LLDPOrganizationalTLV.OUI_LENGTH+LLDPOrganizationalTLV.SUBTYPE_LENGTH;
    private static final short DOMAIN_LENGTH=LLDPOrganizationalTLV.OUI_LENGTH+LLDPOrganizationalTLV.SUBTYPE_LENGTH;

    public static final byte HCP_CHASSIS_SUBTYPE=7;
    public static final byte HCP_PORTID_SUBTYPE=2;
    public static final byte HCP_DOMAINID_TYPE=9;
    public static final byte HCP_DOMAINID_SUBTYPE=7;
    public static final byte HCP_VPORTID_TYPE=10;
    public static final byte HCP_VPORTID_SUBTYPE=2;

    public static final short HCP_TL_LENGTH=2;

    private final byte[] ttlvalue=new byte[]{0,0x78};

    public HCPLLDP(){
        setTtl(new LLDPTLV().setType(TTL_TLV_TYPE)
        .setLength((short)ttlvalue.length)
        .setValue(ttlvalue));
    }

    public HCPLLDP(LLDP lldp){
        this.portId=lldp.getPortId();
        this.chassisId=lldp.getChassisId();
        this.ttl=lldp.getChassisId();
        this.optionalTLVList=lldp.optionalTLVList;
    }

    public void setChassisId(long dpid){
        byte [] chassis= ArrayUtils.addAll(new byte[]{HCP_CHASSIS_SUBTYPE},
                (HCP_CHASSIS_VALUE_PREFIX+String.format("%016x",dpid)).getBytes());
        LLDPTLV chassisTLV=new LLDPTLV();
        chassisTLV.setLength((short)chassis.length);
        chassisTLV.setType(CHASSIS_TLV_TYPE);
        chassisTLV.setValue(chassis);
        this.setChassisId(chassisTLV);
    }

    public void setPortId(int portNum){
        byte [] port=ArrayUtils.addAll(new byte[]{HCP_PORTID_SUBTYPE},
                ByteBuffer.allocate(4).putInt(portNum).array());
        LLDPTLV portTLV=new LLDPTLV();
        portTLV.setLength((short)port.length);
        portTLV.setType(PORT_TLV_TYPE);
        portTLV.setValue(port);
        this.setPortId(portTLV);
    }

    public void setDomainId(long domainId){
        byte [] domain=ArrayUtils.addAll(new byte[]{HCP_DOMAINID_SUBTYPE},
                    (HCP_DOMAINID_VALUE_PREFIX+String.format("%016x",domainId)).getBytes());
        LLDPTLV domainTLV=new LLDPTLV();
        domainTLV.setLength((short)domain.length);
        domainTLV.setType(HCP_DOMAINID_TYPE);
        domainTLV.setValue(domain);
        optionalTLVList.add(domainTLV);
    }

    public void setVportId(int vportNumber){
        byte [] vport=ArrayUtils.addAll(new byte[]{HCP_VPORTID_SUBTYPE},
                    ByteBuffer.allocate(4).putInt(vportNumber).array());
        LLDPTLV vportTLV=new LLDPTLV();
        vportTLV.setLength((short)vport.length);
        vportTLV.setType(HCP_VPORTID_TYPE);
        vportTLV.setValue(vport);
        optionalTLVList.add(vportTLV);
    }

    public void setHCPLLDPName(String name){
        LLDPOrganizationalTLV nameTlV=new LLDPOrganizationalTLV();
        nameTlV.setLength((short)(name.length()+NAME_LENGTH));
        nameTlV.setInfoString(name);
        nameTlV.setSubType(NAME_SUBTYPE);
        nameTlV.setOUI(ONLAB_OUI);
        optionalTLVList.add(nameTlV);
    }

    public LLDPOrganizationalTLV getNameTLV(){
        for (LLDPTLV lldptlv:this.optionalTLVList){
            if (lldptlv.getType()==LLDPOrganizationalTLV.ORGANIZATIONAL_TLV_TYPE){
                LLDPOrganizationalTLV organizationalTLV=(LLDPOrganizationalTLV)lldptlv;
                if (organizationalTLV.getSubType()==NAME_LENGTH){
                    return organizationalTLV;
                }
            }
        }
        return null;
    }

    public String getNameString(){
        LLDPOrganizationalTLV tlv=getNameTLV();
        if (tlv!=null){
            return new String(tlv.getInfoString(), StandardCharsets.UTF_8);
        }
        return null;
    }

    public String getDpid(){
        LLDPTLV chassiTLV=getChassisId();
        byte [] chassis=chassiTLV.getValue();
        ByteBuffer chassisBuffer=ByteBuffer.wrap(chassis);
        chassisBuffer.position(1);
        byte[] dpidbytes=new byte[chassis.length-1];
        chassisBuffer.get(dpidbytes);
        String dpidStr=new String(dpidbytes,StandardCharsets.UTF_8);
        if (dpidStr.startsWith(HCP_CHASSIS_VALUE_PREFIX)){
            return dpidStr.substring(HCP_CHASSIS_VALUE_PREFIX.length());
        }
        return null;
    }

    public int getPortNum(){
        LLDPTLV portTLV=getPortId();
        byte[] port=portTLV.getValue();
        ByteBuffer bb=ByteBuffer.wrap(port);
        bb.position(1);
        return bb.getInt();
    }

    public Long getDomianId(){
        for (LLDPTLV tlv:this.optionalTLVList){
            if (tlv.getType()==HCP_DOMAINID_TYPE){
                byte[] domain=tlv.getValue();
                ByteBuffer domainBuffer=ByteBuffer.wrap(domain);
                domainBuffer.position(1);
                byte [] domainBytes=new byte[domain.length-1];
                domainBuffer.get(domainBytes);
                String domainIdStr=new String(domainBytes,StandardCharsets.UTF_8);
                if (domainIdStr.startsWith(HCP_DOMAINID_VALUE_PREFIX)){
                    return Long.valueOf(domainIdStr.substring(HCP_DOMAINID_VALUE_PREFIX.length()),16);
                }
            }
        }
        return null;
    }

    public Integer getVportNum(){
        for (LLDPTLV lldptlv:this.getOptionalTLVList()){
            if (lldptlv.getType()==HCP_VPORTID_TYPE){
                ByteBuffer bb=ByteBuffer.wrap(lldptlv.getValue());
                bb.position(1);
                return bb.getInt();
            }
        }
        return null;
    }

    public static HCPLLDP parseHCPLLDP(Ethernet ethernet){
        if (ethernet.getEtherType()==Ethernet.TYPE_LLDP){
            HCPLLDP hcplldp=new HCPLLDP((LLDP)ethernet.getPayload());
            if (null!=hcplldp.getDpid()){
                return hcplldp;
            }
        }
        return null;
    }

    public static HCPLLDP hcplldp(long dpid,int portNum,long domainId,int VportNUm){
        HCPLLDP probe=new HCPLLDP();
        probe.setChassisId(dpid);
        probe.setPortId(portNum);
        probe.setDomainId(domainId);
        probe.setVportId(VportNUm);
//        probe.setHCPLLDPName(HCP_ORGANIZATION_NAME);
        return probe;
    }
}

