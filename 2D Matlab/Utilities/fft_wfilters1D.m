function [Dx,Dy] = fft_wfilters1D(Nx,Ny,wtype,ori,scale)
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
switch ori
    case {'LL',3}
        Dx = 1;
        Dy = 1;
        for s = 1:scale           
            Dx = Dx.*HaX;
            HaX = [HaX(1:2:end);HaX(1:2:end)];
            Dy = Dy.*HaY;
            HaY = [HaY(1:2:end),HaY(1:2:end)];            
        end
    case {'HL',2}
        Dx = 1;
        Dy = 1;
        for s = 1:scale-1           
            Dx = Dx.*HaX;
            HaX = [HaX(1:2:end);HaX(1:2:end)];
            Dy = Dy.*HaY;
            HaY = [HaY(1:2:end),HaY(1:2:end)];  
            GaY = [GaY(1:2:end),GaY(1:2:end)];
        end
        Dx = Dx.*HaX;
        Dy = Dy.*GaY;         
    case {'LH',1}
        Dx = 1;
        Dy = 1;
        for s = 1:scale-1           
            Dx = Dx.*HaX;
            HaX = [HaX(1:2:end);HaX(1:2:end)];
            GaX = [GaX(1:2:end);GaX(1:2:end)];
            Dy = Dy.*HaY;
            HaY = [HaY(1:2:end),HaY(1:2:end)];            
        end
        Dx = Dx.*GaX;
        Dy = Dy.*HaY; 
    case {'HH',0}
        Dx = 1;
        Dy = 1;
        for s = 1:scale-1           
            Dx = Dx.*HaX;
            HaX = [HaX(1:2:end);HaX(1:2:end)];
            GaX = [GaX(1:2:end);GaX(1:2:end)];
            Dy = Dy.*HaY;
            HaY = [HaY(1:2:end),HaY(1:2:end)];  
            GaY = [GaY(1:2:end),GaY(1:2:end)];
        end
        Dx = Dx.*GaX;
        Dy = Dy.*GaY;        

end
