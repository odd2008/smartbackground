package com.zuo.smartbackground.service.serviceImpl;

import com.zuo.smartbackground.dao.PatientMapper;
import com.zuo.smartbackground.dao.PatientRecordMapper;
import com.zuo.smartbackground.model.*;
import com.zuo.smartbackground.service.PatientRecordService;
import com.zuo.smartbackground.service.UserService;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class PatientRecorrdServiceImpl implements PatientRecordService {

    @Autowired
    private PatientRecordMapper patientRecordMapper;

    @Autowired
    private PatientMapper patientMapper;
    @Autowired
    private UserService userService;
    @Override
    public List<PatientRecord> getPatientRecordByPatientAccount(String account) {
        Patient patient = userService.getPatientByAccount(account);
        if(patient==null||patient.getPatientId()==null)
        {
            return null;
        }
        PatientRecordExample patientRecordExample = new PatientRecordExample();
        patientRecordExample.createCriteria().andPatientIdEqualTo(patient.getPatientId());
        return patientRecordMapper.selectByExample(patientRecordExample);
    }

    @Override
    public List<PatientRecord> getPatientRecordByDoctorAccount(String account) {
        Doctor doctor = userService.getDoctorByAccount(account);
        if(doctor==null||doctor.getDoctorId()==null)
        {
            return null;
        }
        PatientRecordExample patientRecordExample = new PatientRecordExample();
        patientRecordExample.createCriteria().andDoctorIdEqualTo(doctor.getDoctorId());
        return patientRecordMapper.selectByExample(patientRecordExample);
    }

    @Override
    public int createPatientRecord(PatientRecord patientRecord) {
        PatientRecord patientRecord1 = getPatientRecordBySellf(patientRecord);
        if(patientRecord1!=null){
            patientRecord.setPatientRecordId(patientRecord1.getPatientRecordId());
            return patientRecordMapper.updateByPrimaryKeySelective(patientRecord);
        }
        return patientRecordMapper.insertSelective(patientRecord);
    }

    @Override
    public int deletePatientRecord(long patientRecordId) {
        return patientRecordMapper.deleteByPrimaryKey(patientRecordId);
    }

    @Override
    public int updatePatientRecord(PatientRecord patientRecord) {
        return patientRecordMapper.updateByPrimaryKeySelective(patientRecord);
    }

    @Override
    public PatientRecord getPatientRecordBySellf(PatientRecord patientRecord) {
        PatientRecordExample patientRecordExample = new PatientRecordExample();
        patientRecordExample.createCriteria().andPatientIdEqualTo(patientRecord.getPatientId())
                .andDoctorIdEqualTo(patientRecord.getDoctorId()).andAdmissionTimeEqualTo(patientRecord.getAdmissionTime());
        List<PatientRecord> patientRecords = patientRecordMapper.selectByExample(patientRecordExample);
        if(patientRecords!=null&&patientRecords.size()>0){
            return patientRecords.get(0);
        }
        return null;
    }

    @Override
    public List<PPatientRecord> getPatientPatientRecordByDoctorAccount(String account) {
        Doctor doctor = userService.getDoctorByAccount(account);
        if(doctor==null||doctor.getDoctorId()==null)
        {
            return null;
        }
        PatientRecordExample patientRecordExample = new PatientRecordExample();
        patientRecordExample.createCriteria().andDoctorIdEqualTo(doctor.getDoctorId());
        List<PatientRecord> patientRecords = patientRecordMapper.selectByExample(patientRecordExample);
        List<Integer> pid = new ArrayList<>();
        for(PatientRecord p :patientRecords){
            pid.add(p.getPatientId());
        }
        PatientExample patientExample = new PatientExample();
        patientExample.createCriteria().andPatientIdIn(pid);
        List<Patient> patients = patientMapper.selectByExample(patientExample);

        List<PPatientRecord> list = new ArrayList<>();
        for(PatientRecord pr :patientRecords){
            for(Patient p:patients){
                if(pr.getPatientId()==p.getPatientId()){
                    PPatientRecord pPatientRecord = new PPatientRecord();
                    pPatientRecord.setPatient(p);
                    pPatientRecord.setPatientRecord(pr);
                    list.add(pPatientRecord);
                    break;
                }
            }
        }
        return list;
    }

}
