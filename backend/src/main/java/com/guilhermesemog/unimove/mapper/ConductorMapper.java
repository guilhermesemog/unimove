package com.guilhermesemog.unimove.mapper;

import com.guilhermesemog.unimove.dto.conductor.ConductorCreate;
import com.guilhermesemog.unimove.dto.conductor.ConductorPatch;
import com.guilhermesemog.unimove.dto.conductor.ConductorResponse;
import com.guilhermesemog.unimove.dto.conductor.ConductorUpdate;
import com.guilhermesemog.unimove.model.Conductor;
import com.guilhermesemog.unimove.model.User;
import org.springframework.stereotype.Component;

@Component
public class ConductorMapper {

    private final UserMapper userMapper;

    public ConductorMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Conductor toEntity(ConductorCreate conductorBody, User user) {
        return new Conductor(
                user,
                conductorBody.license(),
                conductorBody.licenseExpirationDate()
        );
    }

    public ConductorResponse toResponse(Conductor conductor) {
        return new ConductorResponse(
                userMapper.toResponse(conductor.getUser()),
                conductor.getLicense(),
                conductor.getLicenseExpirationDate()
        );
    }

    public Conductor update(ConductorUpdate newConductor, Conductor conductor) {
        conductor.setUser(userMapper.updateUser(newConductor.user(), conductor.getUser()));
        conductor.setLicense(newConductor.license());
        conductor.setLicenseExpirationDate(newConductor.licenseExpirationDate());

        return conductor;
    }

    public Conductor update(ConductorPatch newConductor, Conductor conductor) {
        if (newConductor.user() != null) {
            conductor.setUser(userMapper.updateUser(newConductor.user(), conductor.getUser()));
        }
        if (newConductor.license() != null) {
            conductor.setLicense(newConductor.license());
        }
        if (newConductor.licenseExpirationDate() != null) {
            conductor.setLicenseExpirationDate(newConductor.licenseExpirationDate());
        }

        return conductor;
    }
}
