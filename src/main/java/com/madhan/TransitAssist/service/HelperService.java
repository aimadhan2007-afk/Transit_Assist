package com.madhan.TransitAssist.service;

import com.madhan.TransitAssist.dto.HelperDTO;
import com.madhan.TransitAssist.exception.BusinessRuleException;
import com.madhan.TransitAssist.exception.ResourceNotFoundException;
import com.madhan.TransitAssist.model.Helper;
import com.madhan.TransitAssist.model.HelperAvailability;
import com.madhan.TransitAssist.model.RequestStatus;
import com.madhan.TransitAssist.repository.AssistanceRequestRepository;
import com.madhan.TransitAssist.repository.HelperRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HelperService {

    private final HelperRepository helperRepository;
    private final AssistanceRequestRepository requestRepository;

    public HelperService(HelperRepository helperRepository, AssistanceRequestRepository requestRepository) {
        this.helperRepository = helperRepository;
        this.requestRepository = requestRepository;
    }

    public Helper createHelper(HelperDTO dto) {
        Helper helper = new Helper();
        helper.setName(dto.getName().trim());
        helper.setPhone(dto.getPhone().trim());
        helper.setSpecialization(dto.getSpecialization());
        helper.setAvailability(dto.getAvailability() != null ? dto.getAvailability() : HelperAvailability.AVAILABLE);
        return helperRepository.save(helper);
    }

    public Helper updateHelper(Long id, HelperDTO dto) {
        Helper helper = getHelperById(id);
        helper.setName(dto.getName().trim());
        helper.setPhone(dto.getPhone().trim());
        helper.setSpecialization(dto.getSpecialization());
        if (dto.getAvailability() != null) {
            helper.setAvailability(dto.getAvailability());
        }
        return helperRepository.save(helper);
    }

    @Transactional(readOnly = true)
    public Helper getHelperById(Long id) {
        return helperRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Helper not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Helper> getAllHelpers() {
        return helperRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Helper> getAvailableHelpers() {
        return helperRepository.findByAvailability(HelperAvailability.AVAILABLE);
    }

    public void deleteHelper(Long id) {
        Helper helper = getHelperById(id);
        boolean hasActiveRequests = helper.getRequests().stream()
                .anyMatch(r -> r.getStatus() == RequestStatus.ASSIGNED || r.getStatus() == RequestStatus.REQUESTED);
        if (hasActiveRequests) {
            throw new BusinessRuleException("Cannot delete helper with active or pending assigned requests.");
        }
        helperRepository.delete(helper);
    }

    public Helper updateAvailability(Long id, HelperAvailability availability) {
        Helper helper = getHelperById(id);
        helper.setAvailability(availability);
        return helperRepository.save(helper);
    }

    @Transactional(readOnly = true)
    public long countByAvailability(HelperAvailability availability) {
        return helperRepository.countByAvailability(availability);
    }

    @Transactional(readOnly = true)
    public long countTotalHelpers() {
        return helperRepository.count();
    }
}
