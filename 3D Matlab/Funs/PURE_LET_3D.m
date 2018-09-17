function [output, time] = PURE_LET_3D(input, PSF, options)
%%PURE_LET_3D: PURE-LET 3D deconvolution algorithm
%
% USAGE: [output, time] = PURE_LET_3D(input, PSF, options)
%
% INPUT:
%        'input'        -  the input blurred noisy image to be processed
%        'PSF'          -  the point-spread function (PSF), same size as input
%        'options'      -  algorithm settings
%           'numIte'    -  number of depth of the wavelet, default 3
%           'numWiener' -  number of Wiener filter, default 3
%           'oracle'    -  oracle test (MSE-LET), default 0
%           'debug'     -  whether to show the figure, default 0
%
% OUTPUT:
%         'output'      -  the deconvolved 3D image
%         'time'        -  the running time of deconvolution
%
% EXAMPLE:
% -------------------------------------------------------------------------
% % load the original image
% I0 = aux_stackread('Pollen.tif'); 
% 
% % load the PSF, which has the same size as the original image
% PSF = aux_stackread('PSF.tif'); 
% 
% % noise settings: 
% %   [alpha] scaling factor of Poisson noise; 
% %   [nsigma^2] variance of Gaussian noise;  
% options.alpha = 0.2; options.nsigma = 0.2;
% 
% % generate the measurement
% [input, options] = aux_acquisition(I0, PSF, options);
% 
% % PURE-LET image deconvolution
% output = PURE_LET_3D(input, PSF, options);
% 
% % visualize the measurement and deconvolved image
% aux_sliceViewer(input,output);
% -------------------------------------------------------------------------
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

tic;
[nx,ny,nz] = size(input);

if min(input(:))>10
    input = input - min(input(:));
end

if nargin<2
    error('Please provide the image and PSF.');
end

if nargin==2
    disp('Default settings loaded.')
    % default settings
    options.numWiener = 3;
    options.numIte = 3;
    
    [alpha, sigma] = aux_estNoisePar(input);
    %     options.alpha = aux_estAlpha(input);% noise levels
    options.alpha = alpha;
    options.nsigma = sigma;
    %     disp(['Finished. alpha = ' num2str(alpha) ', sigma = ' num2str(sigma)]);
else
    if ~isfield(options, 'numWiener')
        options.numWiener = 3;
    end
    if ~isfield(options, 'numIte')
        options.numIte = 3;
    end
end

disp('--------------------    Begin deconvolution    --------------------');
% for memory consideration, use single precision
Y = single(input);
PSF = PSF./sum(PSF(:));
H = fftn(fftshift(PSF));
H = single(H);

S = aux_reg(nx, ny, nz);
numWiener = options.numWiener;
alpha = options.alpha;
sigma = options.nsigma;
J = options.numIte;

Ht     = conj(H);
H2     = Ht.*H;
S2     = conj(S).*S;
sigma2 = sigma^2;

Ey = mean2(Y);
En2 = alpha*Ey + sigma2;
beta   = 1e-5*En2;
lambda = logspace(-3,-2,numWiener)*En2;
Hbi    = Ht./(H2+beta*S2);
Hbit   = conj(Hbi);

clear H options S input;

Y      = fftn(Y);
Ybi = Hbi.*Y;

O = 7; %number of subbands
N = nx*ny*nz;
L = numWiener;

wtype = 'haar';
X=cell(J,O);
iterations=size(X);
for ix=1:prod(iterations)
    [j, o]=ind2sub(iterations,ix);
    [tmpDx, tmpDy, tmpDz] = ...
        fft_wfilters1D_3D(nx,ny,nz,wtype,o-1,j);
    Dx(j,o,:) = tmpDx;
    Dy(j,o,:) = tmpDy;
    Dz(j,o,:) = tmpDz;
end

[Dlx,Dly,Dlz] = fft_wfilters1D_3D(nx,ny,nz,wtype,O,J);

DDl = single(Dlx*Dly);
Dl = bsxfun(@times, DDl, reshape(Dlz,1,1,[]));
Rl = conj(Dl)/8^J;
clear DDl;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
F1   = zeros(N,J*O*L,'single');
F2   = zeros(N,J*O*L,'single');
div1 = zeros(J*O*L,1,'single');
div2 = zeros(J*O*L,1,'single');

X=cell(L,J,O);

iterations=size(X);
Yl  = 0;
% %% Low-Low subband
for li = 1:L
    Hli{li} = Ht./(H2+lambda(li)*S2);
    Yi{li}  = Hli{li}.*Y;
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    % Reconstruction of the Lowpass Residual:
    %----------------------------------------
    Fkl = (Rl.*Dl).*Yi{li};
    Yl = Yl+Fkl/L;
end

clear Dl Rl Fkl H2 Hbi Ht S2 X;

% for ix=1:prod(iterations)

ix = 1;
for l = 1 : L
    disp([num2str(l) '/' num2str(L) 'th Wiener filter']);
    for j = 1 : J
        disp(['    depth: ' num2str(j) '/' num2str(J)]);
        for o = 1 : O
            disp(['        wavelet subband: ' num2str(o) '/' num2str(O)]);
            %     [l, j, o]=ind2sub(iterations,ix);
            [ff1, ff2, d1, d2] = eleFun(Hbit, Y, Hli{l}, Yi{l}, sigma2, Dx(j,o,:), Dy(j,o,:), Dz(j,o,:), alpha,j);
            if ~isempty(d1)
                F1(:,ix) = single(ff1);
                F2(:,ix) = single(ff2);
                div1(ix) = d1;
                div2(ix) = d2;
                ix = ix+1;
            end
            
        end
    end
end

disp(['PURE Minimization (' num2str(numel(div1)*2) ' coefficients)']);

clearvars -except F1 F2 div1 div2 Ybi Yl nx ny nz N;

F   = cat(2,F1,F2);
div = cat(1,div1,div2);
clear F1 F2 div1 div2;

minY = Ybi - Yl;
minY = minY(:);

% %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
A  = real(F'*F)/N^2;

% Global Image-domain PURE Minimization:
%---------------------------------------
c = real(F'*(minY(:)))/N^2-div/N;
% c = real(sum(F.*minY))/N^2-div'/N;
c = max(c,0);
% Ie = eye(k);
% mu = 5e-2;
% a2 = (A+mu*Ie)\c;
a2 = pinv(A)*c;

clearvars -except F a2 Yl nx ny nz;

Fa = F*a2;

output = abs(ifftn(reshape(Fa,nx, ny, nz)+Yl));

time = toc;

if nargout < 2
    disp('------------------------------');
    disp(['Running Time : ' num2str(time) ' s']);
end

end