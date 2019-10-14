package io.mosip.kernel.masterdata.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.masterdata.constant.MOSIPDeviceServiceErrorCode;
import io.mosip.kernel.masterdata.dto.MOSIPDeviceServiceDto;
import io.mosip.kernel.masterdata.dto.MOSIPDeviceServiceExtDto;
import io.mosip.kernel.masterdata.entity.MOSIPDeviceService;
import io.mosip.kernel.masterdata.entity.MOSIPDeviceServiceHistory;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.DeviceProviderRepository;
import io.mosip.kernel.masterdata.repository.MOSIPDeviceServiceHistoryRepository;
import io.mosip.kernel.masterdata.repository.MOSIPDeviceServiceRepository;
import io.mosip.kernel.masterdata.repository.RegistrationDeviceSubTypeRepository;
import io.mosip.kernel.masterdata.repository.RegistrationDeviceTypeRepository;
import io.mosip.kernel.masterdata.service.MOSIPDeviceServices;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;

/**
 * 
 * @author Megha Tanga
 * 
 */
@Service
public class MOSIPDeviceServiceImpl implements MOSIPDeviceServices {

	@Autowired
	MOSIPDeviceServiceRepository mosipDeviceServiceRepository;

	@Autowired
	MOSIPDeviceServiceHistoryRepository mosipDeviceServiceHistoryRepository;

	@Autowired
	RegistrationDeviceTypeRepository registrationDeviceTypeRepository;

	@Autowired
	RegistrationDeviceSubTypeRepository registrationDeviceSubTypeRepository;

	@Autowired
	DeviceProviderRepository deviceProviderRepository;

	@Override
	@Transactional
	public MOSIPDeviceServiceExtDto createMOSIPDeviceService(MOSIPDeviceServiceDto dto) {
		MOSIPDeviceService mosipDeviceService = null;
		MOSIPDeviceService entity = null;
		MOSIPDeviceServiceHistory entityHistory = null;
		try {
			if (dto != null) {

				if (mosipDeviceServiceRepository.findById(MOSIPDeviceService.class, dto.getId()) != null) {
					throw new RequestException(MOSIPDeviceServiceErrorCode.MDS_EXIST.getErrorCode(),
							String.format(MOSIPDeviceServiceErrorCode.MDS_EXIST.getErrorMessage(), dto.getId()));
				}
				if ((registrationDeviceTypeRepository.findByCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(
						dto.getRegDeviceTypeCode())) == null) {
					throw new RequestException(MOSIPDeviceServiceErrorCode.REG_DEVICE_TYPE_NOT_FOUND.getErrorCode(),
							MOSIPDeviceServiceErrorCode.REG_DEVICE_TYPE_NOT_FOUND.getErrorMessage());
				}
				if ((registrationDeviceSubTypeRepository.findByCodeAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(
						dto.getRegDeviceSubCode())) == null) {
					throw new RequestException(MOSIPDeviceServiceErrorCode.REG_DEVICE_SUB_TYPE_NOT_FOUND.getErrorCode(),
							MOSIPDeviceServiceErrorCode.REG_DEVICE_SUB_TYPE_NOT_FOUND.getErrorMessage());
				}
				if ((deviceProviderRepository.findByIdAndIsDeletedFalseorIsDeletedIsNullAndIsActiveTrue(
						dto.getDeviceProviderId())) == null) {
					throw new RequestException(MOSIPDeviceServiceErrorCode.DEVICE_PROVIDER_NOT_FOUND.getErrorCode(),
							MOSIPDeviceServiceErrorCode.DEVICE_PROVIDER_NOT_FOUND.getErrorMessage());
				}

				entity = MetaDataUtils.setCreateMetaData(dto, MOSIPDeviceService.class);
				entity.setIsActive(true);
				entityHistory = MetaDataUtils.setCreateMetaData(dto, MOSIPDeviceServiceHistory.class);
				entityHistory.setEffectDateTime(entity.getCreatedDateTime());
				entityHistory.setCreatedDateTime(entity.getCreatedDateTime());
				mosipDeviceService = mosipDeviceServiceRepository.create(entity);
				mosipDeviceServiceHistoryRepository.create(entityHistory);
			}

		} catch (DataAccessLayerException | DataAccessException | IllegalArgumentException
				| SecurityException exception) {
			throw new MasterDataServiceException(MOSIPDeviceServiceErrorCode.MDS_INSERTION_EXCEPTION.getErrorCode(),
					MOSIPDeviceServiceErrorCode.MDS_INSERTION_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(exception));
		}
		return MapperUtils.map(mosipDeviceService, MOSIPDeviceServiceExtDto.class);

	}

}