
package com.forum.directory.repo.kafka.listener;

import com.forum.directory.kafka.event.Directories;
import com.forum.directory.kafka.event.Directory;
import com.forum.directory.kafka.event.OperationDirectoryKafka;
import com.forum.directory.repo.mapper.DirectoryMapper;
import com.forum.directory.repo.model.DirectoryThemeEntity;
import com.forum.directory.repo.repository.DirectoryPagingRepository;
import com.forum.directory.repo.repository.DirectoryRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component

public class DirectoryListener {

    @Autowired
    private DirectoryRepository directoryRepository;
    @Autowired
    private DirectoryPagingRepository directoryPagingRepository;
    private DirectoryMapper mapper = DirectoryMapper.INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryListener.class);

    @KafkaListener(topics = "${kafka.topic.product.request}", containerFactory = "requestReplyListenerContainerFactory")
    @SendTo
    public Directories listenConsumerRecord(ConsumerRecord<String, Directories> record) {//, Consumer<String,Directories> consumerToMoveOffset){

        long secondsToSleep = 3;

        LOGGER.info("Start");

        //print all headers
        record.headers().forEach(header -> LOGGER.debug(header.key() + ":" + new String(header.value())));

        String key = record.key();
        Directories directories = record.value();
        LOGGER.debug("Listen; key : " + key);
        LOGGER.debug("Listen; value : " + directories);

        Directories directoriesToReturn = resolveAndExecute(directories);

        LOGGER.info("Ending");
        return directoriesToReturn;

    }

    private Directories resolveAndExecute(Directories directories) {

        LOGGER.info("Start");
        Directories directoriesToReturn = null;

        if (directories.getOperation().equals(OperationDirectoryKafka.RETREIVE_DETAILS)) {
            directoriesToReturn = getDirectory(directories);
        } else if (directories.getOperation().equals(OperationDirectoryKafka.RETREIVE_ALL)) {
            directoriesToReturn = getAllDirectories(directories);
        } else if (directories.getOperation().equals(OperationDirectoryKafka.CREATE)) {
            directoriesToReturn = createDirectories(directories);
        } else if (directories.getOperation().equals(OperationDirectoryKafka.UPDATE)) {
            directoriesToReturn = updateDirectories(directories);
        } else if (directories.getOperation().equals(OperationDirectoryKafka.DELETE)) {
            directoriesToReturn = deleteDirectories(directories);
        } else if (directories.getOperation().equals(OperationDirectoryKafka.GET_MAX_TOPIC_ID)) {
            directoriesToReturn = maxTopicId();//directories.getDirectories().get(0).getTopicId());
        } else {
            LOGGER.debug("Inside else. Undefined Operation!");
        }
        LOGGER.info("Ending");
        return directoriesToReturn;
    }
    @Transactional
    @Cacheable(cacheNames="directory")
    public Directories getDirectory(Directories directories) {

        LOGGER.info("Start");
        Directories directoriesToReturn = new Directories();
        if ((null != directories) && (directories.getDirectories().iterator().hasNext())) {
            Long directoryId = ((Directory) directories.getDirectories().iterator().next()).getDirectoryId();
            LOGGER.debug("Fetching Directory with directoryId : {}", directoryId);

//            DirectoryThemeEntity directoryThemeEntity = directoryRepository.findById((directoryId)).get(); //findById(Long.parseLong(directoryId)).get();
            Optional<DirectoryThemeEntity>directoryFoundOptional = directoryRepository.findById(directoryId);
            if ( directoryFoundOptional.isPresent()) {
                LOGGER.debug("Directory with directoryId : {} found in repository", directoryId);
                DirectoryThemeEntity directoryThemeEntity = directoryFoundOptional.get();
                List<Directory> directoryListToReturn = new ArrayList<Directory>();
                directoryListToReturn.add(mapper.entityToApi(directoryThemeEntity));
                directoriesToReturn.setDirectories(directoryListToReturn);
                directoriesToReturn.setOperation(OperationDirectoryKafka.SUCCESS);

            } else {
            //if (directoryThemeEntity == null) {
                LOGGER.debug("Directory with directoryId : {} not found in repository", directoryId);

                directoriesToReturn.setOperation(OperationDirectoryKafka.FAILURE);
            }

        } else {
            LOGGER.debug("Directory cannot be fetched, since param is null or empty");
            directoriesToReturn.setOperation(OperationDirectoryKafka.FAILURE);
        }
        LOGGER.info("Ending");
        return directoriesToReturn;
    }
    @Transactional
    @Cacheable(cacheNames="directory")
    public Directories createDirectories(Directories directories) {

        LOGGER.info("Start");
        Directories directoriesToReturn = new Directories();
        List<Directory> directoryListToReturn = null;
        Directory directoryToCreate = null;
        List<DirectoryThemeEntity> directoriesFound = null;
        DirectoryThemeEntity directoryCreatedOR = null;
//        Integer page = directoriesToReturn.getPage();
//        Integer numberPerPage = directoriesToReturn.getNumberPerPage();
//        if (page == null) {
//            page = 0;
//        }
//        if (numberPerPage == null) {
//            numberPerPage = 1000;
//        }

        if ((null != directories) && (directories.getDirectories().iterator().hasNext())) {

            directoryToCreate = directories.getDirectories().iterator().next();
            LOGGER.debug("Attempting to create a new Directory with code: {}", directoryToCreate.getName());

//            directoriesFound = directoryPagingRepository.findByName(directoryToCreate.getName(), PageRequest.of(page, numberPerPage));
//            if (directoriesFound.size() > 0) {
//                LOGGER.debug("A Directory with code {} already exist", directoriesFound.iterator().next().getName());
//                directoriesToReturn.setOperation(OperationDirectoryKafka.FAILURE);
//            } else {
                //DirectoryThemeEntity dirEmptyEntity = new DirectoryThemeEntity();
                DirectoryThemeEntity directoryFromKafkaEntity = mapper.apiToEntity(directoryToCreate);

                //BeanUtils.copyProperties(directoryFromKafkaEntity, dirEmptyEntity,
                //    getNullPropertyNames(directoryFromKafkaEntity));
                //System.out.println("dirEmptyEntity = "+dirEmptyEntity);
                directoryCreatedOR = directoryRepository.save(directoryFromKafkaEntity);       //dirEmptyEntity);
                LOGGER.debug("A Directory with id {} created newly", directoryCreatedOR.getDirectoryId());
                directoriesToReturn.setOperation(OperationDirectoryKafka.SUCCESS);
                directoryListToReturn = new ArrayList<Directory>();
                directoryListToReturn.add(mapper.entityToApi(directoryCreatedOR));
                directoriesToReturn.setDirectories(directoryListToReturn);
//            }
        } else {
            LOGGER.debug("Directory cannot be created, since param is null or empty");
            directoriesToReturn.setOperation(OperationDirectoryKafka.FAILURE);
        }
        LOGGER.info("Ending");
        return directoriesToReturn;
    }
    @Transactional
    @CachePut(cacheNames="directory")
    public Directories updateDirectories(Directories directories) {

        LOGGER.info("Start");
        Directories directoriesToReturn = new Directories();
        List<Directory> directoryListToReturn = null;
        Directory directoryToUpdate = null;
        DirectoryThemeEntity directoryFoundEntity = null;
        DirectoryThemeEntity directoryUpdatedEntity = null;

        if ((null != directories) && (directories.getDirectories().iterator().hasNext())) {

            directoryToUpdate = directories.getDirectories().iterator().next();
            LOGGER.debug("Attempting to find a Directory with id: {} to update", directoryToUpdate.getDirectoryId());

//            directoryFoundEntity = directoryRepository.findById((directoryToUpdate.getDirectoryId())).get();//findById(Long.parseLong(directoryToUpdate.getDirectoryId())).get();
            Optional<DirectoryThemeEntity>directoryFoundOptional = directoryRepository.findById((directoryToUpdate.getDirectoryId()));
            if (directoryFoundOptional.isPresent()) {
//            if (null != directoryFoundEntity) {
                directoryFoundEntity = directoryFoundOptional.get();
                LOGGER.debug("A Directory with id {} exist, attempting to update", directoryFoundEntity.getDirectoryId());
                directoriesToReturn.setOperation(OperationDirectoryKafka.SUCCESS);
                DirectoryThemeEntity dirConvertEntity = mapper.apiToEntity(directoryToUpdate);
//                dirConvertEntity.setCreationDate(null);
                BeanUtils.copyProperties(dirConvertEntity, directoryFoundEntity, getNullPropertyNames(dirConvertEntity));
                directoryUpdatedEntity = directoryRepository.save(directoryFoundEntity); // here was change from dirConvertEntity
                directoryListToReturn = new ArrayList<Directory>();
                directoryListToReturn.add(mapper.entityToApi(directoryUpdatedEntity));
                directoriesToReturn.setDirectories(directoryListToReturn);
            } else {
                LOGGER.debug("A Directory with id {} doesn't exist", directoryToUpdate.getDirectoryId());
                directoriesToReturn.setOperation(OperationDirectoryKafka.FAILURE);
            }
        } else {
            LOGGER.debug("Directory cannot be updated, since param is null or empty");
            directoriesToReturn.setOperation(OperationDirectoryKafka.FAILURE);
        }
        LOGGER.info("Ending");
        return directoriesToReturn;
    }
    @Transactional
    @CacheEvict(cacheNames="directory")
    public Directories deleteDirectories(Directories directories) {

        LOGGER.info("Start");
        Directories directoriesToReturn = new Directories();
        List<Directory> directoryListToReturn = null;
        Directory directoryToDelete = null;
        DirectoryThemeEntity directoryFoundEntity = null;

        if ((null != directories) && (directories.getDirectories().iterator().hasNext())) {

            directoryToDelete = directories.getDirectories().iterator().next();
            LOGGER.debug("Attempting to find a Directory with id: {} to delete", directoryToDelete.getDirectoryId());

            Optional<DirectoryThemeEntity>directoryFoundOptional = directoryRepository.findById((directoryToDelete.getDirectoryId()));
            if (directoryFoundOptional.isPresent()) {
                directoryFoundEntity = directoryFoundOptional.get();
                LOGGER.debug("A Directory with id {} exist, attempting to delete", directoryFoundEntity.getDirectoryId());
                directoryRepository.delete(mapper.apiToEntity(directoryToDelete));
                directoriesToReturn.setOperation(OperationDirectoryKafka.SUCCESS);
                directoryListToReturn = new ArrayList<Directory>();
                directoriesToReturn.setDirectories(directoryListToReturn);
            } else {
                LOGGER.debug("A Directory with id {} doesn't exist", directoryToDelete.getDirectoryId());
                directoriesToReturn.setOperation(OperationDirectoryKafka.FAILURE);
            }
        } else {
            LOGGER.debug("Directory cannot be deleted, since param is null or empty");
            directoriesToReturn.setOperation(OperationDirectoryKafka.FAILURE);
        }
        LOGGER.info("Ending");
        return directoriesToReturn;
    }
    @Transactional
    @Cacheable(cacheNames="directory")
    public Directories getAllDirectories(Directories directories) {

        LOGGER.info("Start");
        Directories directoriesToReturn = new Directories();
        Integer page = directories.getPage();
        Integer numberPerPage = directories.getNumberPerPage();
        if (page == null) {
            page = 0;
        }
        if (numberPerPage == null) {
            numberPerPage = 1000;
        }
        Page<DirectoryThemeEntity> directoryEntityPages = directoryPagingRepository.findAllByOrderByOrderAsc(PageRequest.of(page, numberPerPage));
        if( !directoryEntityPages.hasContent()){
            directoriesToReturn.setOperation(OperationDirectoryKafka.FAILURE);
            return directoriesToReturn;
        }
        //Iterable<DirectoryThemeEntity> iterable = directoryRepository.findAll();

        List<Directory> directoryListToReturn = new ArrayList<Directory>();
        for (DirectoryThemeEntity directoryThemeEntity : directoryEntityPages) {
            directoryListToReturn.add(mapper.entityToApi(directoryThemeEntity));
        }
        if (directoryListToReturn.size() == 0) {
            LOGGER.debug("No directories retreived from repository");
        }
        directoryListToReturn.forEach(item -> LOGGER.debug(item.toString()));

        directoriesToReturn.setOperation(OperationDirectoryKafka.SUCCESS);
        directoriesToReturn.setDirectories(directoryListToReturn);

        //delay();
        LOGGER.info("Ending");
        return directoriesToReturn;
    }

    private Directories maxTopicId(){//Long topicId) {
        LOGGER.info("Start");
        Directories directoriesToReturn = new Directories();
        Optional<DirectoryThemeEntity> optionalDirThemeEntity = directoryRepository.findTopByOrderByTopicIdDesc();
        if( !optionalDirThemeEntity.isPresent()){
            directoriesToReturn.setOperation(OperationDirectoryKafka.FAILURE);
            return directoriesToReturn;
        }
        DirectoryThemeEntity dirEntity=optionalDirThemeEntity.get();
        Long maxTopic= dirEntity.getTopicId();
        //Long maxTopic = directoryRepository.findTopByOrderByTopicIdDesc();//findTopByTopicId();//topicId);
        directoriesToReturn.setOperation(OperationDirectoryKafka.SUCCESS);
        directoriesToReturn.setMax(maxTopic);
        LOGGER.info("Ending");
        return directoriesToReturn;
    }
    //    https://stackoverflow.com/questions/27818334/jpa-update-only-specific-fields
    public static String[] getNullPropertyNames (Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for(java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
}
