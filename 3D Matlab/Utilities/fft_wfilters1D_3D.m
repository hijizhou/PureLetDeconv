function [Dx, Dy, Dz] = fft_wfilters1D_3D(Nx,Ny,Nz, wtype,ori,scale)
% Works for orthonormal wavelet filters only
%
% AUTHORS: Jizhou Li, Florian Luisier and Thierry Blu
%
% REFERENCES:
%     [1] J. Li, F. Luisier and T. Blu, PURE-LET image deconvolution, 
%           IEEE Trans. Image Process., vol. 27, no. 1, pp. 92-105, 2018.
%     [2] J. Li, F. Luisier and T. Blu, Deconvolution of Poissonian images with the PURE-LET approach, 
%           2016 23rd Proc. IEEE Int. Conf. on Image Processing (ICIP 2016), Phoenix, Arizona, USA, 2016, pp.2708-2712.
%     [3] J. Li, F. Luisier and T. Blu, PURE-LET deconvolution of 3D fluorescence microscopy images, 
%           2017 14th Proc. IEEE Int. Symp. Biomed. Imaging (ISBI 2017), Melbourne, Australia, 2017, pp. 723-727.
%
% CONTACT: Jizhou Li (hijizhou@gmail.com), The Chinese University of Hong Kong.
%
% Last updated: 08 Nov, 2017

match = 1;
FaX = fft_wavefilters(Nx,wtype,match);
HaX = FaX(1,:)';
GaX = FaX(2,:)';
FaY = fft_wavefilters(Ny,wtype,match);
HaY = conj(FaY(1,:));
GaY = conj(FaY(2,:));
FaZ = fft_wavefilters(Nz,wtype,match);
HaZ = FaZ(1,:)';
GaZ = FaZ(2,:)';
% HaZ = conj(FaZ(1,:)');
% GaZ = conj(FaZ(2,:)');

switch ori
    case {'LLL',7}
        Dx = 1;
        Dy = 1;
        Dz = 1;
        for s = 1:scale
            Dx = Dx.*HaX;
            HaX = [HaX(1:2:end);HaX(1:2:end)];
            Dy = Dy.*HaY;
            HaY = [HaY(1:2:end),HaY(1:2:end)];
            Dz = Dz.*HaZ;
            HaZ = [HaZ(1:2:end);HaZ(1:2:end)];
        end
    case {'LLH',6}
        Dx = 1;
        Dy = 1;
        Dz = 1;
        for s = 1:scale-1
            Dx = Dx.*HaX;
            HaX = [HaX(1:2:end);HaX(1:2:end)];
            Dy = Dy.*HaY;
            HaY = [HaY(1:2:end),HaY(1:2:end)];
            Dz = Dz.*HaZ;
            HaZ = [HaZ(1:2:end);HaZ(1:2:end)];
            GaZ = [GaZ(1:2:end);GaZ(1:2:end)];
        end
        Dx = Dx.*HaX;
        Dy = Dy.*HaY;
        Dz = Dz.*GaZ;
    case {'HLL',5}
        Dx = 1;
        Dy = 1;
        Dz = 1;
        for s = 1:scale-1
            Dx = Dx.*HaX;
            HaX = [HaX(1:2:end);HaX(1:2:end)];
            GaX = [GaX(1:2:end);GaX(1:2:end)];
            Dy = Dy.*HaY;
            HaY = [HaY(1:2:end),HaY(1:2:end)];
            Dz = Dz.*HaZ;
            HaZ = [HaZ(1:2:end);HaZ(1:2:end)];
        end
        Dx = Dx.*GaX;
        Dy = Dy.*HaY;
        Dz = Dz.*HaZ;
    case {'HLH',4}
        Dx = 1;
        Dy = 1;
        Dz = 1;
        for s = 1:scale-1
            Dx = Dx.*HaX;
            HaX = [HaX(1:2:end);HaX(1:2:end)];
            GaX = [GaX(1:2:end);GaX(1:2:end)];
            Dy = Dy.*HaY;
            HaY = [HaY(1:2:end),HaY(1:2:end)];
            Dz = Dz.*HaZ;
            HaZ = [HaZ(1:2:end);HaZ(1:2:end)];
            GaZ = [GaZ(1:2:end);GaZ(1:2:end)];
        end
        Dx = Dx.*GaX;
        Dy = Dy.*HaY;
        Dz = Dz.*GaZ;
    case {'LHL',3}
        Dx = 1;
        Dy = 1;
        Dz = 1;
        for s = 1:scale-1
            Dx = Dx.*HaX;
            HaX = [HaX(1:2:end);HaX(1:2:end)];
            Dy = Dy.*HaY;
            HaY = [HaY(1:2:end),HaY(1:2:end)];
            GaY = [GaY(1:2:end),GaY(1:2:end)];
            Dz = Dz.*HaZ;
            HaZ = [HaZ(1:2:end);HaZ(1:2:end)];
        end
        Dx = Dx.*HaX;
        Dy = Dy.*GaY;
        Dz = Dz.*HaZ;
    case {'LHH',2}
        Dx = 1;
        Dy = 1;
        Dz = 1;
        for s = 1:scale-1
            Dx = Dx.*HaX;
            HaX = [HaX(1:2:end);HaX(1:2:end)];
            Dy = Dy.*HaY;
            HaY = [HaY(1:2:end),HaY(1:2:end)];
            GaY = [GaY(1:2:end),GaY(1:2:end)];
            Dz = Dz.*HaZ;
            HaZ = [HaZ(1:2:end);HaZ(1:2:end)];
            GaZ = [GaZ(1:2:end);GaZ(1:2:end)];
        end
        Dx = Dx.*HaX;
        Dy = Dy.*GaY;
        Dz = Dz.*GaZ;
    case {'HHL',1}
        Dx = 1;
        Dy = 1;
        Dz = 1;
        for s = 1:scale-1
            Dx = Dx.*HaX;
            HaX = [HaX(1:2:end);HaX(1:2:end)];
            GaX = [GaX(1:2:end);GaX(1:2:end)];
            Dy = Dy.*HaY;
            HaY = [HaY(1:2:end),HaY(1:2:end)];
            GaY = [GaY(1:2:end),GaY(1:2:end)];
            Dz = Dz.*HaZ;
            HaZ = [HaZ(1:2:end);HaZ(1:2:end)];
        end
        Dx = Dx.*GaX;
        Dy = Dy.*GaY;
        Dz = Dz.*HaZ;
        
    case {'HHH',0}
        Dx = 1;
        Dy = 1;
        Dz = 1;
        for s = 1:scale-1
            Dx = Dx.*HaX;
            HaX = [HaX(1:2:end);HaX(1:2:end)];
            GaX = [GaX(1:2:end);GaX(1:2:end)];
            Dy = Dy.*HaY;
            HaY = [HaY(1:2:end),HaY(1:2:end)];
            GaY = [GaY(1:2:end),GaY(1:2:end)];
            Dz = Dz.*HaZ;
            HaZ = [HaZ(1:2:end);HaZ(1:2:end)];
            GaZ = [GaZ(1:2:end);GaZ(1:2:end)];
        end
        Dx = Dx.*GaX;
        Dy = Dy.*GaY;
        try
        Dz = Dz.*GaZ;
        catch
            Dz
        end
            
end
