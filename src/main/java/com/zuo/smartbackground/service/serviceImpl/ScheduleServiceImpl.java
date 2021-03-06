package com.zuo.smartbackground.service.serviceImpl;

import com.zuo.smartbackground.dao.BookMapper;
import com.zuo.smartbackground.dao.DoctorMapper;
import com.zuo.smartbackground.dao.ScheduleMapper;
import com.zuo.smartbackground.model.*;
import com.zuo.smartbackground.service.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Service
public class ScheduleServiceImpl implements ScheduleService{

    private Logger logger = LoggerFactory.getLogger(ScheduleServiceImpl.class);

    @Autowired
    private ScheduleMapper scheduleMapper;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private DoctorMapper doctorMapper;
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public int arrangeSchedule(Schedule schedule) {

        int x = -1;
        if (schedule.getW()==0){
            schedule.setW(1);
            ScheduleExample scheduleExample = new ScheduleExample();
            scheduleExample.createCriteria().andDoctorIdEqualTo(schedule.getDoctorId())
                    .andWEqualTo(schedule.getW())
                    .andWorkTimeStartEqualTo(schedule.getWorkTimeStart());
            List<Schedule> schedules = scheduleMapper.selectByExample(scheduleExample);
            if(schedules==null||schedules.size()==0){
                logger.info("没有时间冲突、正在插入schedule");
                x = x + scheduleMapper.insertSelective(schedule);
            }else{
                logger.info("有时间冲突");
                for(Schedule s:schedules){
                    logger.info("冲突时间："+s.getWorkTimeStart().toString());
                }
            }

            if(x==0){
                schedule.setW(2);
                ScheduleExample scheduleExample1 = new ScheduleExample();
                scheduleExample1.createCriteria().andDoctorIdEqualTo(schedule.getDoctorId())
                        .andWEqualTo(schedule.getW())
                        .andWorkTimeStartEqualTo(schedule.getWorkTimeStart());
                List<Schedule> schedules1 = scheduleMapper.selectByExample(scheduleExample1);
                if(schedules1==null||schedules1.size()==0){
                    logger.info("没有时间冲突、正在插入schedule");
                    x = x + scheduleMapper.insertSelective(schedule);
                }else{
                    logger.info("有时间冲突");
                    for(Schedule s:schedules){
                        logger.info("冲突时间："+s.getWorkTimeStart().toString());
                    }
                }

            }
        }else{
            ScheduleExample scheduleExample = new ScheduleExample();
            scheduleExample.createCriteria().andDoctorIdEqualTo(schedule.getDoctorId())
                    .andWEqualTo(schedule.getW())
                    .andWorkTimeStartEqualTo(schedule.getWorkTimeStart());
            List<Schedule> schedules = scheduleMapper.selectByExample(scheduleExample);
            if(schedules==null||schedules.size()==0){
                logger.info("没有时间冲突、正在插入schedule");
                return  scheduleMapper.insertSelective(schedule);
            }else{
                logger.info("有时间冲突");
                for(Schedule s:schedules){
                    logger.info("冲突时间："+s.getWorkTimeStart().toString());
                }
            }

        }

        return x;

    }

    @Override
    public int arrangeScheduleList(List<Schedule> schedules) {
        int i = 0;
        for(Schedule s:schedules)
        {
            i = scheduleMapper.insertSelective(s);
        }
        return i;
    }

    @Override
    public int cancleSchedule(Schedule schedule) {
        return scheduleMapper.updateByPrimaryKeySelective(schedule);
    }

    @Override
    public int cancleScheduleList(List<Schedule> schedules)
    {
        int i = 0;
        for(Schedule s:schedules)
        {
            i = scheduleMapper.updateByPrimaryKeySelective(s);
        }
        return i;
    }



    @Override
    public int makeSche(MakeSchedule makeSchedule) {
        Schedule schedule = new Schedule();
        schedule.setRemainder(makeSchedule.getRemainder());
        schedule.setDoctorId(makeSchedule.getDoctorId());
        schedule.setWorkTimeStart(makeSchedule.getDate());
        return 0;
    }

    @Override
    public List<Schedule> getScheduleByDoctorID(int doctorID) {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andDoctorIdEqualTo(doctorID);
        return scheduleMapper.selectByExample(scheduleExample);
    }

    @Override
    public List<Schedule> getScheduleBySessionID(int sectionID) {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andDoctorIdIn(doctorMapper.selectBySectionId(sectionID))
                .andWorkTimeStartGreaterThan(new Date(new Date().getTime()));
        return scheduleMapper.selectByExample(scheduleExample);
    }

    @Override
    public List<Schedule> getAllSchedule() {
//        ScheduleExample scheduleExample = new ScheduleExample();
//        scheduleExample.createCriteria().andIsCancleEqualTo(false);
        return scheduleMapper.selectByExample(null);
    }

    @Override
    public List<Schedule> getAllFromNowSchedule() {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andWorkTimeStartGreaterThanOrEqualTo(new Date());
        return scheduleMapper.selectByExample(scheduleExample);
    }

    @Override
    public List<Schedule> getValiScheduleByDoctorId(int doctorID) {
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria()
                .andWorkTimeStartGreaterThanOrEqualTo(new Date(new Date().getTime()+3600*2))
                .andDoctorIdEqualTo(doctorID);
        return scheduleMapper.selectByExample(scheduleExample);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public int deleteSchedule(int scheduleId) {
        Schedule schedule = new Schedule();
        schedule.setIsCancle(true);
        schedule.setScheduleId(scheduleId);
        int w = scheduleMapper.updateByPrimaryKeySelective(schedule);
        if(w==1){
            BookExample bookExample = new BookExample();
            bookExample.createCriteria().andIsCancleEqualTo(false)
                    .andScheduleIdEqualTo(scheduleId);
            Book book = new Book();
            book.setIsCancle(true);
            book.setIsAvaliablity(false);
            int k = bookMapper.updateByExampleSelective(book,bookExample);
//            return k;
        }
        return w;
//        return scheduleMapper.deleteByPrimaryKey(scheduleId);
    }

    @Override
    public int updateSchedule(Schedule schedule) {
        return scheduleMapper.updateByPrimaryKeySelective(schedule);
    }

    @Override
    public List<Schedule> getScheduleByBookList(List<Book> books) {
        List<Integer> scids = new ArrayList<>();
        if(books==null||books.size()<1){
            return null;
        }
        for(Book book:books){
            scids.add(book.getScheduleId());
        }
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andScheduleIdIn(scids);
        return scheduleMapper.selectByExample(scheduleExample);
    }

    @Override
    public List<Schedule> getScheduleByDoctorAccount(String account) {
        DoctorExample doctorExample = new DoctorExample();
        doctorExample.createCriteria().andAccountEqualTo(account);
        List<Doctor> doctors  = doctorMapper.selectByExample(doctorExample);
        if(doctors==null||doctors.size()<1){
            return null;
        }
        ScheduleExample scheduleExample = new ScheduleExample();
        scheduleExample.createCriteria().andDoctorIdEqualTo(doctors.get(0).getDoctorId());
        return scheduleMapper.selectByExample(scheduleExample);
    }
}
